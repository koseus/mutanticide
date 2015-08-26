// NEED TO TEST FOR
// 2ND CS IN A 2-CS METHOD
// 2ND CS IN A 3-CS METHOD



import static org.objectweb.asm.Opcodes.*;

import java.io.*;
import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class RSBMutation {
	final String className;
	final String methodToMutate;
	final int lineToMutate;


	// Labels to denote the start of the tryCatchBlocks of SB to remove
	Label mStartLabel1, mStartLabel2;

	// List to hold the instructions of the method
	InsnList insnList = new InsnList();

	InsnList updatedInsnList = new InsnList();

	// List to hold the methods of the class with their instructions
	List<MethodNode> methodList = new ArrayList<MethodNode>();

	public RSBMutation(String name, String method, int line){
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

				updateInsn();

				// Remove the TryCatchBlocks of the SB to remove
				for(int i = 0; i < tryCatchBlocks.size(); i++){
					TryCatchBlockNode tcbn = (TryCatchBlockNode) tryCatchBlocks.get(i);
					if(tcbn.start.getLabel().equals(mStartLabel1) ||
							tcbn.start.getLabel().equals(mStartLabel2)){
						tryCatchBlocks.remove(i);

						// Decrement counter to avoid skipping the next element
						--i;
					}
				}
			}
		}

		public void visitTryCatchBlock(Label start, Label end, Label handler, String type){
			super.visitTryCatchBlock(start, end, handler, type);

		}
	}

	public void RSB() throws IOException{
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

	private void updateInsn() {
		int encounter = 0;
		updatedInsnList.clear();

		for(int i = 0; i < insnList.size(); i++){
			AbstractInsnNode currentInsn = insnList.get(i);
			updatedInsnList.add(currentInsn);

			if(currentInsn.getType() == 15){
				LineNumberNode lnn = (LineNumberNode) currentInsn;
				int lineNumber = lnn.line;

				// Actual mutation code
				if(lineNumber == lineToMutate){
					++encounter;

					// First part of our Critical Section
					if(encounter == 1){
						i += 4;

						// Start of the first TryCatchBlock
						mStartLabel1 = ((LabelNode) insnList.get(i + 1)).getLabel();
					// Second part of our Critical Section
					}else if(encounter == 2){
						// There are 4 code snippets to skip. Labels should stay in place,
						// because they appear in the TryCatchBlocks visited

						// Skip the first snippet
						i += 3;
						// First label
						updatedInsnList.add(insnList.get(i));

						// Skip the second snippet
						i += 2;
						// Second label
						updatedInsnList.add(insnList.get(i));
						mStartLabel2 = ((LabelNode) insnList.get(i)).getLabel();

						// Frame
						//updatedInsnList.add(insnList.get(i + 1));

						// Skip the third snippet
						i += 4;
						// Third label
						updatedInsnList.add(insnList.get(i));

						// Skip the fourth snippet
						i += 1;

						// And go on
					}
				}
				else{
					AbstractInsnNode nextInsn = insnList.get(i + 1);
					if(encounter == 2 && nextInsn.getType() == 14){
						// Skip FRAME CHOP
						++i;
						++encounter;
					}
				}

			}
		}
	}
}
