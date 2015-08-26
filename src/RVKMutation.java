
import java.io.*;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

public class RVKMutation {
	final String className;
	final String fieldName;

	public RVKMutation(String name, String field){
		className = name;
		fieldName = field;
	}

    public class RVKClassWriter extends ClassVisitor{

        public RVKClassWriter(ClassWriter cv) {
            super(ASM5, cv);
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value){
        	if(name.equals(fieldName)){
        		return super.visitField(access - ACC_VOLATILE, name, desc, signature, value);
        	}else{
        		return super.visitField(access, name, desc, signature, value);
        	}
		}
    }

    public void RVK() throws IOException{
    	InputStream in = Main.class.getResourceAsStream(className + ".class");
    	ClassReader classReader = new ClassReader(in);
    	ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

    	// Wrap the ClassWriter with our custom ClassVisitor
    	RVKClassWriter mcw = new RVKClassWriter(cw);
    	classReader.accept(mcw, 0);

    	// Write the output to a class file
    	File outputDir = new File("../out/RVK_" + fieldName);

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
