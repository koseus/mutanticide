// Throws error for method invocations without parameter, since this operator modifies the function's parameter
// Need to check for that before creating the operator

import static org.objectweb.asm.Opcodes.*;

import java.io.*;
import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class MXTMutation {
	final String className;
	final String methodToMutate;
	final int lineToMutate;
	
	// List to hold the instructions of the method
	InsnList insnList = new InsnList();
	
	InsnList updatedInsnList = new InsnList();
	
	// List to hold the methods of the class with their instructions
	List<MethodNode> methodList = new ArrayList<MethodNode>();
	
	
	
	public MXTMutation(String name, String method, int line){
		className = name;
		methodToMutate = method;
		lineToMutate = line;
	}
	
	public class ClassNodeVisitor extends ClassNode{

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
	
	public void MXT() throws IOException{
		InputStream in = Main.class.getResourceAsStream(className + ".class");
	    ClassReader classReader = new ClassReader(in);
	    ClassNodeVisitor classNode = new ClassNodeVisitor();

	    classReader.accept(classNode, 0);
	    in.close();
	    
	    // Now that we have the instruction list of the method to mutate in hand,
	    // and we know which line to mutate,
	    // go to that line and replace the instruction list with ours
	    
//	    updateInsn();
	    

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
	    
	    // Add method myWait
	    
	    File outDir = new File("out/MXT_" + lineToMutate + "/");
	    outDir.mkdirs();
	    DataOutputStream dout = new DataOutputStream(new FileOutputStream(new File(outDir, className + ".class")));
	    dout.write(cw.toByteArray());
	    dout.flush();
	    dout.close();	    
	}

	private void updateInsn() {
		updatedInsnList.clear();
		
		for(int i = 0; i < insnList.size(); i++){
			AbstractInsnNode currentInsn = insnList.get(i);
			updatedInsnList.add(currentInsn);
			
			if(currentInsn.getType() == 15){
				LineNumberNode lnn = (LineNumberNode) currentInsn;
				int lineNumber = lnn.line;
				
				if(lineNumber == lineToMutate){
					updatedInsnList.add(insnList.get(i + 1));
					updatedInsnList.add(insnList.get(i + 2));
					updatedInsnList.add(new LdcInsnNode(new Long(2)));
					updatedInsnList.add(new InsnNode(LDIV));
					updatedInsnList.add(insnList.get(i + 3));
					i += 3;
				}			
			}
		}	
	}
}