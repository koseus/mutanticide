

import static org.objectweb.asm.Opcodes.*;

import java.io.*;
import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;


public class EXCRMutation {

	final String className;
	final String methodToMutate;
	final int lineToMutate;


	// List to hold the instructions of the method
	InsnList insnList = new InsnList();

	InsnList updatedInsnList = new InsnList();



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

	public EXCRMutation(String name, String method, int line){
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

				findPrevNextLines();

				if(previousLine != -1 && nextLine != -1){
					updateInsn();
				}else if(previousLine != -1){
					updateInsn1();
				}else if(nextLine != -1){
					updateInsn2();
				}else{
					// No mutant can be created
				}



				for(int i = 0; i < tryCatchBlocks.size(); i++){
					TryCatchBlockNode tcbn = (TryCatchBlockNode) tryCatchBlocks.get(i);
					if(tcbn.start.getLabel().equals(oldStartLabel)){
						newEndLabel = tcbn.end.getLabel();
						newHandlerLabel = tcbn.handler.getLabel();
						tryCatchBlocks.remove(i);
						tryCatchBlocks.add(i, new TryCatchBlockNode(new LabelNode(newStartLabel), new LabelNode(newEndLabel), new LabelNode(newHandlerLabel), null));
					}
				}
			}
		}
	}

	public void EXCR() throws IOException{
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

	    File outDir = new File("out/");
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
//		System.out.println(previousLine);

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
//		System.out.println(nextLine);
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


	// Expand up and down
	private void updateInsn() {
		InsnList preInsnList = new InsnList();
		InsnList csBeginInsnList = new InsnList();
		InsnList csInsnList1 = new InsnList();
		InsnList csInsnList2 = new InsnList();
		InsnList csInsnList3 = new InsnList();
		InsnList csInsnList4 = new InsnList();
		InsnList csEndInsnList1 = new InsnList();
		InsnList csEndInsnList2 = new InsnList();
		InsnList postInsnList = new InsnList();

		int i;

		for(i = 0; i < insnList.size(); i++){
			preInsnList.add(insnList.get(i));

			// LineNumberNode
			if(insnList.get(i).getType() == 15){
				LineNumberNode lnn = (LineNumberNode) insnList.get(i);

				if(lnn.line == previousLine){
					break;
				}
			}
		}

		while(insnList.get(++i).getType() != 8){
			csInsnList2.add(insnList.get(i));
		}

		// LabelNode
		csInsnList1.add(insnList.get(i));
		// New start label of our tryCatchBlock
		newStartLabel = ((LabelNode) insnList.get(i)).getLabel();
		// LineNumberNode
		csInsnList1.add(insnList.get(++i));

		// 4 instructions of csBegin
		for(int j = 0; j < 4; j++){
			csBeginInsnList.add(insnList.get(++i));
		}

		// Old start label of our tryCatchBlock
		oldStartLabel = ((LabelNode) insnList.get(++i)).getLabel();

		while(true){
			if(insnList.get(i + 1).getType() == 15){
				LineNumberNode lnn = (LineNumberNode) insnList.get(i + 1);

				if(lnn.line == lineToMutate){
					break;
				}
			}

			csInsnList3.add(insnList.get(i++));
		}

		// First part of csEndInsnList
		for(int j = 0; j < 5; j++){
			AbstractInsnNode ain = insnList.get(i++);

			if(ain.getType() == 15){
				LineNumberNode lnn = (LineNumberNode) ain;

				// Increment line number by 1, since we have expanded the CS up by 1 line
				--lnn.line;

				csEndInsnList1.add(lnn);
			}else{
				csEndInsnList1.add(ain);
			}
		}

		// Skip GoTo, will add it manually later
		++i;

		// Second part of csEndInsnList
		for(int j = 0; j < 6; j++){
			csEndInsnList2.add(insnList.get(i++));
		}

		// LabelNode
		csInsnList4.add(insnList.get(i++));
		// LineNumberNode
		csInsnList4.add(insnList.get(i++));

		// Skip Frame Chop
		FrameNode fn = (FrameNode) insnList.get(i++);

		while(insnList.get(i).getType() != 8){
			csInsnList4.add(insnList.get(i++));
		}

		// LabelNode
		LabelNode gotoLabel = (LabelNode) insnList.get(i);
		postInsnList.add(insnList.get(i++));
		// LineNumberNode
		postInsnList.add(insnList.get(i++));

		// Frame Chop
		postInsnList.add(fn);

		for(; i < insnList.size(); i++){
			postInsnList.add(insnList.get(i));
		}

		updatedInsnList.add(preInsnList);
		updatedInsnList.add(csBeginInsnList);
		updatedInsnList.add(csInsnList1);
		updatedInsnList.add(csInsnList2);
		updatedInsnList.add(csInsnList3);
		updatedInsnList.add(csInsnList4);
		updatedInsnList.add(csEndInsnList1);
		updatedInsnList.add(new JumpInsnNode(Opcodes.GOTO, gotoLabel));
		updatedInsnList.add(csEndInsnList2);
		updatedInsnList.add(postInsnList);
	}

	// Expand up
	private void updateInsn1(){
		InsnList preInsnList = new InsnList();
		InsnList csBeginInsnList = new InsnList();
		InsnList csInsnList1 = new InsnList();
		InsnList csInsnList2 = new InsnList();
		InsnList csInsnList3 = new InsnList();
		InsnList csInsnList4 = new InsnList();
		InsnList csEndInsnList = new InsnList();
		InsnList postInsnList = new InsnList();


		int i;

		for(i = 0; i < insnList.size(); i++){
			preInsnList.add(insnList.get(i));

			// LineNumberNode
			if(insnList.get(i).getType() == 15){
				LineNumberNode lnn = (LineNumberNode) insnList.get(i);

				if(lnn.line == previousLine){
					break;
				}
			}
		}

		while(insnList.get(++i).getType() != 8){
			csInsnList2.add(insnList.get(i));
		}

		// LabelNode
		csInsnList1.add(insnList.get(i));
		// New start label of our tryCatchBlock
		newStartLabel = ((LabelNode) insnList.get(i)).getLabel();
		// LineNumberNode
		csInsnList1.add(insnList.get(++i));

		// 4 instructions of csBegin
		for(int j = 0; j < 4; j++){
			csBeginInsnList.add(insnList.get(++i));
		}

		// Old start label of our tryCatchBlock
		oldStartLabel = ((LabelNode) insnList.get(++i)).getLabel();

		while(true){
			if(insnList.get(i + 1).getType() == 15){
				LineNumberNode lnn = (LineNumberNode) insnList.get(i + 1);

				if(lnn.line == lineToMutate){
					break;
				}
			}

			csInsnList3.add(insnList.get(i++));
		}

		// Instruction set for End CS
		for(int j = 0; j < 12; j++){
			AbstractInsnNode ain = insnList.get(i++);

			if(ain.getType() == 15){
				LineNumberNode lnn = (LineNumberNode) ain;

				// Increment line number by 1, since we have expanded the CS up by 1 line
				--lnn.line;

				csEndInsnList.add(lnn);
			}else{
				csEndInsnList.add(ain);
			}
		}

		for(; i < insnList.size(); i++){
			postInsnList.add(insnList.get(i));
		}

		updatedInsnList.add(preInsnList);
		updatedInsnList.add(csBeginInsnList);
		updatedInsnList.add(csInsnList1);
		updatedInsnList.add(csInsnList2);
		updatedInsnList.add(csInsnList3);
		updatedInsnList.add(csInsnList4);
		updatedInsnList.add(csEndInsnList);
		updatedInsnList.add(postInsnList);
	}

	// Expand down
	private void updateInsn2(){
		InsnList csBeginInsnList = new InsnList();
		InsnList csInsnList1 = new InsnList();
		InsnList csInsnList2 = new InsnList();
		InsnList csEndInsnList1 = new InsnList();
		InsnList csEndInsnList2 = new InsnList();
		InsnList postInsnList = new InsnList();

		int i = 0;

		// Instruction list begins with csBegin
		for(int j = 0; j < 6; j++){
			csBeginInsnList.add(insnList.get(i++));
		}

		// Add instructions to csInsnList1 up to the end of CS
		while(true){
			AbstractInsnNode ain = insnList.get(i + 1);

			if(ain.getType() == 15){
				LineNumberNode lnn = (LineNumberNode) ain;

				if(lnn.line == lineToMutate){
					break;
				}
			}

			csInsnList1.add(insnList.get(i++));
		}

		// First part of csEndInsnList
		for(int j = 0; j < 5; j++){
			csEndInsnList1.add(insnList.get(i++));
		}

		// Skip GoTo, will add it manually later
		++i;

		// Second part of csEndInsnList
		for(int j = 0; j < 6; j++){
			csEndInsnList2.add(insnList.get(i++));
		}

		// LabelNode
		csInsnList2.add(insnList.get(i++));
		// LineNumberNode
		LineNumberNode lnn = (LineNumberNode) insnList.get(i++);
		--lnn.line;
		csInsnList2.add(lnn);

		FrameNode fn = (FrameNode) insnList.get(i++);

		while(insnList.get(i).getType() != 8){
			csInsnList2.add(insnList.get(i++));
		}

		// LabelNode
		LabelNode gotoLabel = (LabelNode) insnList.get(i);
		postInsnList.add(insnList.get(i++));
		// LineNumberNode
		postInsnList.add(insnList.get(i++));

		postInsnList.add(fn);


		for(; i < insnList.size(); i++){
			postInsnList.add(insnList.get(i));
		}


		updatedInsnList.add(csBeginInsnList);
		updatedInsnList.add(csInsnList1);
		updatedInsnList.add(csInsnList2);
		updatedInsnList.add(csEndInsnList1);
		updatedInsnList.add(new JumpInsnNode(Opcodes.GOTO, gotoLabel));
		updatedInsnList.add(csEndInsnList2);
		updatedInsnList.add(postInsnList);


	}
}
