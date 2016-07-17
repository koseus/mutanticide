
import java.io.*;


import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;


import static org.objectweb.asm.Opcodes.*;

public class ASKMutation extends Mutation{

	
	public ASKMutation(String name, String method){
		super(name, method);
	}

    //Our class modifier class visitor. It delegate all calls to the super class
    //Only makes sure that it returns our MethodVisitor for every method
    class ClassNodeVisitor extends ClassNode{
        String methodToMutate;

        public ClassNodeVisitor(String method) {
            super(ASM5);         
            
            methodToMutate = method;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
	        	if(name.equals(methodToMutate)){
	        		return super.visitMethod(access + ACC_SYNCHRONIZED, name, desc, signature, exceptions);
	        	}else{
	        		return super.visitMethod(access, name, desc, signature, exceptions);
	        	}
	    }
    }

    public void ASK() throws IOException{
    	InputStream in = Main.class.getResourceAsStream(className + ".class");
	    ClassReader classReader = new ClassReader(in);
	    ClassNodeVisitor classNode = new ClassNodeVisitor(methodToMutate);

	    classReader.accept(classNode, 0);
	    in.close();

	    // Write the modified class to file
	    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
	    
	    classNode.accept(cw);
	    
	    File outDir = new File("out/ASK_" + methodToMutate + "/");
	    outDir.mkdirs();
	    DataOutputStream dout = new DataOutputStream(new FileOutputStream(new File(outDir, className + ".class")));
	    dout.write(cw.toByteArray());
	    dout.flush();
	    dout.close();
    }
}
