
import static org.objectweb.asm.Opcodes.*;

import java.io.*;
import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class SPCRMutation {
	final String className;
	final String methodToMutate;
	final int lineToMutate;


	// List to hold the instructions of the method
	InsnList insnList = new InsnList();

	InsnList updatedInsnList = new InsnList();

	LabelNode startLabel1;
	LabelNode endLabel1;
	LabelNode handlerLabel1;

	LabelNode startLabel2;
	LabelNode endLabel2;
	LabelNode handlerLabel2;


	int csLineCount = 0;

	int lastCSLine = 0;

	// List to hold the methods of the class with their instructions
	List<MethodNode> methodList = new ArrayList<MethodNode>();

	public SPCRMutation(String name, String method, int line){
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

			if(name.equals("main")){
				insnList = this.instructions;
			}

			// If we are in our method to mutate
			if(name.equals(methodToMutate)){
				// Copy the instructions of this method into our instruction lab
				insnList = this.instructions;


				findCSLineCount();
				findLastCSLine();

				if(csLineCount >= 2){
					updateInsn();

					tryCatchBlocks.add(new TryCatchBlockNode(startLabel1, endLabel1, handlerLabel1, null));
					tryCatchBlocks.add(new TryCatchBlockNode(startLabel2, endLabel2, handlerLabel2, null));


				}else{
					// No mutant can be created
				}
			}
		}
	}

	public void SPCR() throws IOException{
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

					// Second encounter means the end of CS
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

//		System.out.println(csLineCount);
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

//		System.out.println(lastCSLine);
	}


	private void updateInsn(){
		InsnList preInsnList = new InsnList();
		InsnList csBeginInsnList = new InsnList();
		InsnList firstCSInsnList = new InsnList();
		InsnList secondCSInsnList = new InsnList();
		InsnList csEndInsnList1 = new InsnList();
		InsnList csEndInsnList2 = new InsnList();
		InsnList csEndInsnList3 = new InsnList();
		InsnList csEndInsnList4 = new InsnList();
		InsnList postInsnList = new InsnList();

		int i;

		// Add instructions up to the CS
		for(i = 0; i < insnList.size(); i++){
			preInsnList.add(insnList.get(i));

			AbstractInsnNode ain = insnList.get(i);

			if(ain.getType() == 15){
				LineNumberNode lnn = (LineNumberNode) ain;

				if(lnn.line == lineToMutate){
					break;
				}
			}
		}

//		System.out.println(preInsnList.size());

		// Instruction set for CS begin
		for(int j = 0; j < 4; j++){
			csBeginInsnList.add(insnList.get(++i));
		}

		int numOfLabels = 0;

		// Add half of the instructions to the first CS
		while(numOfLabels < (csLineCount / 2)){
			firstCSInsnList.add(insnList.get(++i));

			if(insnList.get(i).getType() == 8){
				++numOfLabels;
			}
		}

		// Add the last statement of the first part
		while(insnList.get(++i).getType() != 8){
			firstCSInsnList.add(insnList.get(i));
		}

//		System.out.println(firstCSInsnList.size());

		// First CS ends here

		// Start label of the first tryCatchBlock to add
		startLabel1 = (LabelNode) insnList.get(i);

		while(true){
			// Until the end of the original CS
			if(insnList.get(i + 1).getType() == 15){
				LineNumberNode lnn = (LineNumberNode) insnList.get(i + 1);

				if(lnn.line == lineToMutate){
					break;
				}

				secondCSInsnList.add(insnList.get(i++));
			}else{
				AbstractInsnNode ain = insnList.get(i++);
				if(ain.getType() == 15){
					LineNumberNode lnn = (LineNumberNode) ain;

					// Incremenet the line number by 2, since the code will be shifted
					lnn.line += 2;


					secondCSInsnList.add(lnn);
				}else{
					secondCSInsnList.add(ain);
				}
			}
		}

//		System.out.println(secondCSInsnList.size());

		for(int j = 0; j < 5; j++){
			csEndInsnList1.add(insnList.get(i++));
		}

		// Skip GoTo, will add it manually later
		++i;

		for(int j = 0; j < 6; j++){
			csEndInsnList2.add(insnList.get(i++));
		}

		int secondCSLineNumber = lineToMutate + (csLineCount / 2) + 2;

		LabelNode ln = new LabelNode(new Label());
		LineNumberNode lnn = new LineNumberNode(secondCSLineNumber, ln);
		LabelNode gotoLabel = ln;
		csEndInsnList1.add(new JumpInsnNode(Opcodes.GOTO, gotoLabel));


		updatedInsnList.add(preInsnList);
		updatedInsnList.add(csBeginInsnList);
		updatedInsnList.add(firstCSInsnList);
		updatedInsnList.add(csEndInsnList1);
		updatedInsnList.add(csEndInsnList2);

		updatedInsnList.add(ln);
		updatedInsnList.add(lnn);
		updatedInsnList.add(new FrameNode(Opcodes.F_CHOP, 1, null, 0, null));
		updatedInsnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
		updatedInsnList.add(new InsnNode(Opcodes.DUP));
		updatedInsnList.add(new VarInsnNode(Opcodes.ASTORE, 1));
		updatedInsnList.add(new InsnNode(Opcodes.MONITORENTER));

		updatedInsnList.add(secondCSInsnList);

		ln = new LabelNode(new Label());
		lnn = new LineNumberNode(secondCSLineNumber, ln);
		csEndInsnList3.add(ln);
		csEndInsnList3.add(lnn);
		csEndInsnList3.add(new VarInsnNode(Opcodes.ALOAD, 1));
		csEndInsnList3.add(new InsnNode(Opcodes.MONITOREXIT));

		ln = new LabelNode(new Label());
		endLabel1 = ln;
		csEndInsnList3.add(ln);

		// GoTo in between

		ln = new LabelNode(new Label());
		handlerLabel1 = ln;
		startLabel2 = ln;
		handlerLabel2 = ln;
		csEndInsnList4.add(ln);

		csEndInsnList4.add(new FrameNode(Opcodes.F_FULL, 2, new Object[] {className, className}, 1, new Object[] {"java/lang/Throwable"}));
//		csEndInsnList4.add(fn);
		csEndInsnList4.add(new VarInsnNode(Opcodes.ALOAD, 1));
		csEndInsnList4.add(new InsnNode(Opcodes.MONITOREXIT));
		ln = new LabelNode(new Label());
		endLabel2 = ln;
		csEndInsnList4.add(ln);
		csEndInsnList4.add(new InsnNode(Opcodes.ATHROW));


		// LabelNode
		gotoLabel = new LabelNode(new Label());
		csEndInsnList3.add(new JumpInsnNode(Opcodes.GOTO, gotoLabel));
		postInsnList.add(gotoLabel);

		// LineNumberNode
		lnn = new LineNumberNode(lastCSLine + 6, gotoLabel);
		postInsnList.add(lnn);

		// Skip LabelNode and LineNumberNode in insnList
		i += 2;

		// Frame Chop
//		postInsnList.add(new FrameNode(Opcodes.F_CHOP, 1, null, 0, null));



		for(; i < insnList.size(); i++){
			AbstractInsnNode ain = insnList.get(i);

			if(ain.getType() == 15){
				lnn = (LineNumberNode) ain;
				lnn.line += 2;

				postInsnList.add(lnn);
			}else{
				postInsnList.add(ain);
			}
		}

		updatedInsnList.add(csEndInsnList3);
		updatedInsnList.add(csEndInsnList4);
		updatedInsnList.add(postInsnList);
	}
}
