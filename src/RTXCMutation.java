
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;



import static org.objectweb.asm.Opcodes.*;

public class RTXCMutation {
	final String className;
	final String methodToMutate;
	final int lineToMutate;
	
	// List to hold the instructions of the method
	InsnList insnList = new InsnList();
	
	InsnList updatedInsnList = new InsnList();	

	// List to hold the methods of the class with their instructions
	List<MethodNode> methodList = new ArrayList<MethodNode>();
		
    public RTXCMutation(String name, String method, int line){
    	className = name;
		methodToMutate = method;
		lineToMutate = line;
    }

    class ClassNodeVisitor extends ClassNode{

		public ClassNodeVisitor(){
			super(ASM5);
		}
		
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
			return new MethodNodeVisitor(access, name, desc, signature, exceptions);
		}
	}
    
    class MethodNodeVisitor extends MethodNode{		
		public MethodNodeVisitor(int access, String name, String desc, String signature, String[] exceptions){
			super(ASM5, access, name, desc, signature, exceptions);
		}
		
		public void visitEnd(){

			methodList.add(this);
			
//			if(name.equals("main")){
//				insnList = this.instructions;
//			}
			
			// If we are in our method to mutate
			if(name.equals(methodToMutate)){
				// Copy the instructions of this method into our instruction lab
				insnList = this.instructions;
				
				
				updateInsn();
				
			}else{
				insnList = this.instructions;
			}
		}
	}	
    



    public void RTXC() throws IOException{
		InputStream in = Main.class.getResourceAsStream(className + ".class");
	    ClassReader classReader = new ClassReader(in);
	    ClassNodeVisitor classNode = new ClassNodeVisitor();


	    classReader.accept(classNode, 0);
	    in.close();
	    
	    // Now that we have the instruction list of the method to mutate in hand,
	    // and we know which line to mutate,
	    // go to that line and replace the instruction list with ours
	    
	    
	    
	    for(int i = 0; i < methodList.size(); i++){
	    	if(methodList.get(i).name.equals(methodToMutate)){
	    		methodList.get(i).instructions.clear();
	    		methodList.get(i).instructions.add(updatedInsnList); 
	    	}
	    	classNode.methods.add(methodList.get(i));
	    	
	    }
	    
	    // Write the modified class to file
	    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

	    classNode.accept(cw);
	    
	    File outDir = new File("out/RTXC_" + lineToMutate + "/");
	    outDir.mkdirs();
	    DataOutputStream dout = new DataOutputStream(new FileOutputStream(new File(outDir, className + ".class")));
	    dout.write(cw.toByteArray());
	    dout.flush();
	    dout.close();	    
	}
    
    public void updateInsn(){
    	boolean mutate = false;
    	
    	for(int i = 0; i < insnList.size(); i++){
    		if(mutate){
    			if(insnList.get(i).getType() == 8){
    				updatedInsnList.add(insnList.get(i));
    				mutate = false;
    			}
    		}else{
    			
    			
	    		if(insnList.get(i).getType() == 15){
	    			LineNumberNode lnn = (LineNumberNode) insnList.get(i);
	    			
	    			if(lnn.line == lineToMutate){
	    				mutate = true;
	    				continue;
	    			}
	    		}
	    		
	    		updatedInsnList.add(insnList.get(i));
    		}
    	}
    	
//    	System.out.println(insnList.size());
//    	System.out.println(updatedInsnList.size());
    }
}
