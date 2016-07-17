
import static org.objectweb.asm.Opcodes.*;

import java.io.*;
import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class SHCRMutation extends CRMutation {
	
	Label oldStartLabel;
	Label newStartLabel;
	Label newEndLabel;
	Label newHandlerLabel;
	
	// Line numbers of the statements just before and after the CS
	// -1 denotes no statements
	int previousLine = -1;
	int nextLine = -1;
	
	int lastCSLine = 0;
	
	// List to hold the methods of the class with their instructions
	List<MethodNode> methodList = new ArrayList<MethodNode>();
		
	public SHCRMutation(String name, String method, int line){
		super(name, method, line);
	}
	
	public class ClassNodeVisitor extends ClassNode{

		public ClassNodeVisitor(){
			super(ASM5);
		}
		
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
			return new MethodNodeVisitor(access, name, desc, signature, exceptions);
		}
	}
	
	public class MethodNodeVisitor extends MethodNode{		
		public MethodNodeVisitor(int access, String name, String desc, String signature, String[] exceptions){
			super(ASM5, access, name, desc, signature, exceptions);
		}
		
		public void visitEnd(){
			methodList.add(this);
			
			// If we are in our method to mutate
			if(name.equals(methodToMutate)){
				// Copy the instructions of this method into our instruction lab
				insnList = this.instructions;
				
				// Now that we have the instruction list of the method to mutate in hand,
			    // and we know which line to mutate,
			    // go to that line and replace the instruction list with ours

				findPrevNextLines();
				findLastCSLine();
				
				if(previousLine != -1){
					// Shift up
					updateInsn();
				}else if(nextLine != -1){
					// Shift down
					updateInsn1();
				}else{
					// No mutant can be created
				}
				
				
				
				for(int i = 0; i < tryCatchBlocks.size(); i++){
					TryCatchBlockNode tcbn = (TryCatchBlockNode) tryCatchBlocks.get(i);
					if(tcbn.start.getLabel().equals(oldStartLabel)){						
						newEndLabel = tcbn.end.getLabel();
						newHandlerLabel = tcbn.handler.getLabel();
						
						tryCatchBlocks.remove(i);
						tryCatchBlocks.add(i, new TryCatchBlockNode(new LabelNode(newStartLabel), 
								new LabelNode(tcbn.end.getLabel()),
								new LabelNode(tcbn.handler.getLabel()), 
								tcbn.type));
					}
				}
				
			}
		}
	}
	
	public void SHCR() throws IOException{
		InputStream in = Main.class.getResourceAsStream(className + ".class");
	    ClassReader classReader = new ClassReader(in);
	    ClassNodeVisitor classNode = new ClassNodeVisitor();


	    classReader.accept(classNode, 0);
	    in.close();
	    
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
	    
	    File outDir = new File("out/SHCR_" + lineToMutate + "/");
	    outDir.mkdirs();
	    DataOutputStream dout = new DataOutputStream(new FileOutputStream(new File(outDir, className + ".class")));
	    dout.write(cw.toByteArray());
	    dout.flush();
	    dout.close();	    
	}
	
	
	// Method to find the line numbers of the statements just before and just after the CS
	private void findPrevNextLines(){
		int i;
		
		// Find the line number of the statement just before the CS
		for(i = 0; i < insnList.size(); i++){
			AbstractInsnNode ain = insnList.get(i);
			
			if(ain.getType() == 15)
			{
				LineNumberNode lnn = (LineNumberNode) ain;
				
				if(lnn.line == lineToMutate){
					break;
				}
				
				previousLine = lnn.line;
			}
		}
		
		// If the previous line remains -1, then the CS is the first statement in the method
		
		boolean monitorExit = false;
		
		// Find the line number of the statement just after the CS
		for(; i < insnList.size(); i++){
			AbstractInsnNode ain = insnList.get(i);
			
			if(ain.getType() == 0){
				InsnNode in = (InsnNode) ain;
				
				if(in.getOpcode() == Opcodes.MONITOREXIT){
					monitorExit = true;
				}
			}
			
			if(monitorExit){
				if(ain.getType() == 15){
					LineNumberNode lnn = (LineNumberNode) ain;
					
					AbstractInsnNode nextInsn = insnList.get(i + 2);
					
					if(nextInsn.getType() != 0){
						nextLine = lnn.line;
						break;
					}
				}
			}
		}
		
		// If the next line remains -1, then the method ends with the CS
	}
	
	private void findLastCSLine(){
		boolean enteredCS = false;
		
		for(int i = 0; i < insnList.size(); i++){
			if(enteredCS){
				if(insnList.get(i).getType() == 15){
					LineNumberNode lnn = (LineNumberNode) insnList.get(i);
										
					if(lnn.line == lineToMutate){	
						break;
					}
					
					lastCSLine = lnn.line;
				}
			}else{
				if(insnList.get(i).getType() == 15){
					LineNumberNode lnn = (LineNumberNode) insnList.get(i);
					
					if(lnn.line == lineToMutate){
						enteredCS = true;
					}
				}
			}
		}
	}

	// Shift up
	private void updateInsn() {		
		InsnList preInsnList = new InsnList();
		InsnList csBeginInsnList = new InsnList();
		InsnList csInsnList1 = new InsnList();
		InsnList csInsnList2 = new InsnList();
		InsnList csEndInsnList = new InsnList();
		InsnList postInsnList = new InsnList();
		
		int i;
		
		// Add instructions to preInsnList up to previousLine
		for(i = 0; i < insnList.size(); i++){
			preInsnList.add(insnList.get(i));
			
			// LineNumberNode
			if(insnList.get(i).getType() == 15){
				LineNumberNode lnn = (LineNumberNode) insnList.get(i);
				
				if(lnn.line == previousLine){
					++i;
					break;
				}
			}
		}
		
		// Add the previous statement to csInsnList2
		for(; insnList.get(i).getType() != 8; i++){
			csInsnList2.add(insnList.get(i));
		}		
		
		// First statements of new CS
		// LabelNode
		// New start label of out tryCatchBlock
		newStartLabel = ((LabelNode) insnList.get(i)).getLabel();
		csInsnList1.add(insnList.get(i++));
		
		// LineNumberNode
		csInsnList1.add(insnList.get(i++));
		
		// CS Begin instruction set
		for(int j = 0; j < 4; j++){
			csBeginInsnList.add(insnList.get(i++));
		}
		
		// Old start label of our tryCatchBlock
		oldStartLabel = ((LabelNode) insnList.get(i)).getLabel();
		
		
		// Add instructions to csInsnList2 up to lastCSLine
		while(true){
			if(insnList.get(i + 1).getType() == 15){
				LineNumberNode lnn = (LineNumberNode) insnList.get(i + 1);
				
				if(lnn.line == lastCSLine){
					break;
				}
			}
			
			csInsnList2.add(insnList.get(i++));
		}
		
		
		// LabelNode
		LabelNode gotoLabel = (LabelNode) insnList.get(i); 
		postInsnList.add(insnList.get(i++));
		// LineNumberNode
		LineNumberNode lnn = (LineNumberNode) insnList.get(i++);
		lnn.line = nextLine - 1;
		postInsnList.add(lnn);
		// Frame Chop
		postInsnList.add(new FrameNode(Opcodes.F_CHOP, 1, null, 0, null));
				
		for(; insnList.get(i).getType() != 8; i++){
			postInsnList.add(insnList.get(i));
		}
				
		for(int j = 0; j < 12; j++){
			AbstractInsnNode ain = insnList.get(i++);
			
			if(ain.getType() == 15){
				lnn = (LineNumberNode) ain;
				
				lnn.line = previousLine;
				
				csEndInsnList.add(lnn);
			}else if(ain.getType() == 7){
				csEndInsnList.add(new JumpInsnNode(Opcodes.GOTO, gotoLabel));
			}else{
				csEndInsnList.add(ain);
			}
		}
						
		// LabelNode
		postInsnList.add(insnList.get(i++));
		// LineNumberNode
		postInsnList.add(insnList.get(i++));
		// Skip Frame Chop
		++i;
		
		for(int j = i; j < insnList.size(); j++){
			postInsnList.add(insnList.get(j));
		}
		
		
		updatedInsnList.add(preInsnList);
		updatedInsnList.add(csBeginInsnList);
		updatedInsnList.add(csInsnList1);
		updatedInsnList.add(csInsnList2);
		updatedInsnList.add(csEndInsnList);
		updatedInsnList.add(postInsnList);
	}
	
	// Shift down
	private void updateInsn1() {
		InsnList preInsnList = new InsnList();
		InsnList csBeginInsnList1 = new InsnList();
		InsnList csBeginInsnList2 = new InsnList();
		InsnList csInsnList = new InsnList();
		InsnList csEndInsnList1 = new InsnList();
		InsnList csEndInsnList2 = new InsnList();
		InsnList postInsnList = new InsnList();
		
		int i;
		
		// Add instructions to preInsnList up to lineToMutate
		for(i = 0; i < insnList.size(); i++){
			preInsnList.add(insnList.get(i));
			
			// LineNumberNode
			if(insnList.get(i).getType() == 15){
				LineNumberNode lnn = (LineNumberNode) insnList.get(i);
				
				if(lnn.line == lineToMutate){
					break;
				}
			}
		}
		
		
		// Instructions to open a CS
		for(int j = 0; j < 4; j++){
			csBeginInsnList2.add(insnList.get(++i));
		}
		
		// Old start label of our tryCatchBlock
		oldStartLabel = ((LabelNode) insnList.get(i + 1)).getLabel();
		
		// LabelNode
		csBeginInsnList1.add(insnList.get(++i));
		// LineNumberNode
		csBeginInsnList1.add(insnList.get(++i));
		
		// The first CS statement will be shifted up
		while(insnList.get(++i).getType() != 8){
			preInsnList.add(insnList.get(i));
		}
				
		// New start label of out tryCatchBlock
		newStartLabel = ((LabelNode) insnList.get(i)).getLabel();
		
		// Add instructions to csInsnList until CS closing
		while(true){
			if(insnList.get(i + 1).getType() == 15){
				LineNumberNode lnn = (LineNumberNode) insnList.get(i + 1);
				
				if(lnn.line == lineToMutate){
					break;
				}
			}
			
			csInsnList.add(insnList.get(i++));
		}
		
		for(int j = 0; j < 5; j++){
			AbstractInsnNode ain = insnList.get(i++);
			
			if(ain.getType() == 15){
				LineNumberNode lnn = (LineNumberNode) ain;
				
				// Increment line number by 1, since we have shifted the CS down by 1 lines
				++lnn.line;
				
				csEndInsnList1.add(lnn);
			}else{
				csEndInsnList1.add(ain);
			}
		}
		
		// Skip GoTo, it will be added manually later
		++i;
		
		// Add the remaining CS closing
		for(int j = 0; j < 6; j++){
			csEndInsnList2.add(insnList.get(i++));
		}
		
		
		// LabelNode
		csInsnList.add(insnList.get(i++));
		// LineNumberNode
		csInsnList.add(insnList.get(i++));
		// Skip Frame Chop
		FrameNode fn = (FrameNode) insnList.get(i++);
		
		while(insnList.get(i).getType() != 8){
			csInsnList.add(insnList.get(i++));
		}
		
		// LabelNode
		LabelNode gotoLabel = (LabelNode) insnList.get(i);
		postInsnList.add(insnList.get(i++));
		// LineNumberNode
		postInsnList.add(insnList.get(i++));

		// Frame Chop
		postInsnList.add(fn);
		
		for(int j = i; j < insnList.size(); j++){
			postInsnList.add(insnList.get(j));
		}
		
		updatedInsnList.add(preInsnList);
		updatedInsnList.add(csBeginInsnList1);
		updatedInsnList.add(csBeginInsnList2);
		updatedInsnList.add(csInsnList);
		updatedInsnList.add(csEndInsnList1);
		updatedInsnList.add(new JumpInsnNode(Opcodes.GOTO, gotoLabel));
		updatedInsnList.add(csEndInsnList2);
		updatedInsnList.add(postInsnList);
		
	}
}
