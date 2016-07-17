
import java.io.*;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;


import static org.objectweb.asm.Opcodes.*;

public class RVKMutation {
	final String className;
	final String fieldName;
	
	public RVKMutation(String name, String field){
		className = name;
		fieldName = field;
		
//		System.out.println(fieldName);
	}

	class ClassNodeVisitor extends ClassNode{
		final String fieldName;
		
        public ClassNodeVisitor(String field) {
            super(ASM5);
            
            fieldName = field;
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
	    ClassNodeVisitor classNode = new ClassNodeVisitor(fieldName);


	    classReader.accept(classNode, 0);
	    in.close();
	    

	    // Write the modified class to file
	    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
	    
	    classNode.accept(cw);
	    
	    File outDir = new File("out/RVK_" + fieldName + "/");
	    outDir.mkdirs();
	    DataOutputStream dout = new DataOutputStream(new FileOutputStream(new File(outDir, className + ".class")));
	    dout.write(cw.toByteArray());
	    dout.flush();
	    dout.close();	  
    	
    }
}
