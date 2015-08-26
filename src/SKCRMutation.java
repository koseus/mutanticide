

import static org.objectweb.asm.Opcodes.*;

import java.io.*;
import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class SKCRMutation {
	final String className;
	final String methodToMutate;
	final int lineToMutate;


	// Labels to denote the start of the tryCatchBlocks of SB to remove
	Label mStartLabel1, mStartLabel2;

	// List to hold the instructions of the method
	InsnList insnList = new InsnList();

	InsnList updatedInsnList = new InsnList();

	Label oldStartLabel;
	Label newStartLabel;

	int csLineCount = 0;

	int lastCSLine = 0;

	// List to hold the methods of the class with their instructions
	List<MethodNode> methodList = new ArrayList<MethodNode>();

	public SKCRMutation(String name, String method, int line){
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

				findCSLineCount();
				findLastCSLine();

				if(csLineCount >= 3){
					updateInsn();
				}else if(csLineCount >= 2){
					updateInsn1();
				}else{
					// No mutant can be created
				}

				for(int i = 0; i < tryCatchBlocks.size(); i++){
					TryCatchBlockNode tcbn = (TryCatchBlockNode) tryCatchBlocks.get(i);
					if(tcbn.start.getLabel().equals(oldStartLabel)){
						tryCatchBlocks.remove(i);
						tryCatchBlocks.add(i, new TryCatchBlockNode(new LabelNode(newStartLabel),
								new LabelNode(tcbn.end.getLabel()),
								new LabelNode(tcbn.handler.getLabel()),
								tcbn.type));
					}
				}
			}
		}

		public void visitTryCatchBlock(Label start, Label end, Label handler, String type){
			super.visitTryCatchBlock(start, end, handler, type);

		}
	}

	public void SKCR() throws IOException{
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

	// Method to find the number of statements in CS
	private void findCSLineCount(){
		boolean enteredCS = false;

		for(int i = 0; i < insnList.size(); i++){
			if(insnList.get(i).getType() == 15){
				LineNumberNode lnn = (LineNumberNode) insnList.get(i);

				if(enteredCS){
					if(lnn.line == lineToMutate){
						break;
					}

					++csLineCount;

				}else{
					if(lnn.line == lineToMutate){
						enteredCS = true;
					}
				}
			}
		}
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


	// Shrink on both sides
	private void updateInsn(){
		InsnList preInsnList = new InsnList();
		InsnList csBeginInsnList1 = new InsnList();
		InsnList csBeginInsnList2 = new InsnList();
		InsnList csInsnList = new InsnList();
		InsnList csEndInsnList1 = new InsnList();
		InsnList csEndInsnList2 = new InsnList();
		InsnList postInsnList1 = new InsnList();
		InsnList postInsnList2 = new InsnList();

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

		// Instruction set for csBegin
		for(int j = 0; j < 4; j++){
			csBeginInsnList2.add(insnList.get(++i));
		}

		// Old start label of our tryCatchBlock
		oldStartLabel = ((LabelNode) insnList.get(i + 1)).getLabel();

		// LabelNode
		csBeginInsnList1.add(insnList.get(++i));
		// LineNumberNode
		csBeginInsnList1.add(insnList.get(++i));

		while(insnList.get(++i).getType() != 8){
			preInsnList.add(insnList.get(i));
		}

		// New start label of out tryCatchBlock
		newStartLabel = ((LabelNode) insnList.get(i)).getLabel();

		// Add instructions to csInsnList up to lastCSLine
		while(true){
			AbstractInsnNode ain = insnList.get(i + 1);

			if(ain.getType() == 15){
				LineNumberNode lnn = (LineNumberNode) ain;

				if(lnn.line == lastCSLine){
					break;
				}
			}

			csInsnList.add(insnList.get(i++));
		}

		// LabelNode
		LabelNode gotoLabel = (LabelNode) insnList.get(i);
		postInsnList1.add(insnList.get(i++));
		// LineNumberNode
		postInsnList1.add(insnList.get(i++));

		while(insnList.get(i).getType() != 8){
			postInsnList2.add(insnList.get(i++));
		}

		for(int j = 0; j < 5; j++){
			AbstractInsnNode ain = insnList.get(i++);

			if(ain.getType() == 15){
				LineNumberNode lnn = (LineNumberNode) ain;

				// Increment line number by 1, since we have shrank the CS
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
		postInsnList2.add(insnList.get(i++));
		// LineNumberNode
		postInsnList2.add(insnList.get(i++));
		// Frame Chop
		FrameNode fn = (FrameNode) insnList.get(i++);

		for(; i < insnList.size(); i++){
			postInsnList2.add(insnList.get(i));
		}


		updatedInsnList.add(preInsnList);
		updatedInsnList.add(csBeginInsnList1);
		updatedInsnList.add(csBeginInsnList2);
		updatedInsnList.add(csInsnList);
		updatedInsnList.add(csEndInsnList1);
		updatedInsnList.add(new JumpInsnNode(Opcodes.GOTO, gotoLabel));
		updatedInsnList.add(csEndInsnList2);
		updatedInsnList.add(postInsnList1);
		updatedInsnList.add(fn);
		updatedInsnList.add(postInsnList2);
	}

	// Shrink on upper side only
	private void updateInsn1() {
		InsnList preInsnList = new InsnList();
		InsnList csBeginInsnList1 = new InsnList();
		InsnList csBeginInsnList2 = new InsnList();
		InsnList remainingInsnList = new InsnList();

		int i;

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

		for(int j = 0; j < 4; j++){
			csBeginInsnList2.add(insnList.get(++i));
		}

		// Old start label of our tryCatchBlock
		oldStartLabel = ((LabelNode) insnList.get(i + 1)).getLabel();

		// LabelNode
		csBeginInsnList1.add(insnList.get(++i));
		// LineNumberNode
		csBeginInsnList1.add(insnList.get(++i));

		while(insnList.get(++i).getType() != 8){
			preInsnList.add(insnList.get(i));
		}

		// New start label of out tryCatchBlock
		newStartLabel = ((LabelNode) insnList.get(i)).getLabel();

		while(i < insnList.size()){
			AbstractInsnNode ain = insnList.get(i++);

			if(ain.getType() == 15){
				LineNumberNode lnn = (LineNumberNode) ain;

				if(lnn.line == lineToMutate){
					++lnn.line;
				}

				remainingInsnList.add(lnn);
			}else{
				remainingInsnList.add(ain);
			}
		}

		updatedInsnList.add(preInsnList);
		updatedInsnList.add(csBeginInsnList1);
		updatedInsnList.add(csBeginInsnList2);
		updatedInsnList.add(remainingInsnList);
	}
}
