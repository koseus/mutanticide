
import java.io.*;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

@SuppressWarnings("unused")
public class RJSMutation {
	final String className;
	final String methodToMutate;
	final int lineToMutate;

	public RJSMutation(String name, String method, int line){
		className = name;
		methodToMutate = method;
		lineToMutate = line;
	}

	class RJSMethodWriter extends MethodVisitor{
		private String className;
		private String methodName;
		private int lineToMutate;
    		private int lineNumber;

        public RJSMethodWriter(int api, MethodVisitor mv, String methodName, String className,
        		int lineToMutate) {
            super(api, mv);
            this.methodName = methodName;
            this.className = className;
            this.lineToMutate = lineToMutate;

        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf){
        	if(name.equals("join") && lineToMutate == lineNumber){
        		super.visitMethodInsn(INVOKEVIRTUAL, className, "mySleep", "()V", false);

			}else{
				super.visitMethodInsn(opcode, owner, name, desc, itf);
        	}
        }

        public void visitLineNumber(int line, Label start){
        	this.lineNumber = line;
        }
    }

	class RJSClassWriter extends ClassVisitor{
        private int api;
        private int lineToMutate;
        String className;

        public RJSClassWriter(int api, ClassWriter cv, String className, int lineToMutate) {
            super(api, cv);
            this.api = api;
            this.lineToMutate = lineToMutate;
            this.className = className;


            MethodVisitor mv;
    		{
    			mv = this.visitMethod(ACC_PUBLIC, "mySleep", "()V", null, null);
    			mv.visitCode();
    			Label l0 = new Label();
    			mv.visitLabel(l0);
    			mv.visitLdcInsn(new Long(1000L));
        		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
    			mv.visitInsn(RETURN);
    			Label l1 = new Label();
    			mv.visitLabel(l1);
    			mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
    			mv.visitMaxs(0, 1);
    			mv.visitEnd();
    		}
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                String signature, String[] exceptions) {

            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            RJSMethodWriter mvw = new RJSMethodWriter(api, mv, name, className, lineToMutate);
            return mvw;
        }
    }

	public void RJS() throws IOException{
    	InputStream in = Main.class.getResourceAsStream(className + ".class");
    	ClassReader classReader = new ClassReader(in);
    	ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

    	//Wrap the ClassWriter with our custom ClassVisitor
    	RJSClassWriter mcw = new RJSClassWriter(ASM5, cw, className, lineToMutate);
    	classReader.accept(mcw, 0);

    	//Write the output to a class file
    	File outputDir = new File("../out/RJS_" + lineToMutate + "/");
    	outputDir.mkdirs();
    	DataOutputStream dout = new DataOutputStream(
    			new FileOutputStream(
    					new File(outputDir, className + ".class")));
    	dout.write(cw.toByteArray());
    	dout.flush();
    	dout.close();
    	in.close();
    }
}
