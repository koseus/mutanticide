
import java.io.*;
import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class RNNAMutation {
	final String className;
	final String methodName;
	final int lineToMutate;
	
	List<MethodNode> methodList = new ArrayList<MethodNode>();
	
	InsnList updatedInsn = new InsnList();
	
	public RNNAMutation(String className, String methodName, int line){
		this.className = className;
		this.methodName = methodName;
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
		}
	}
	
	public void RNNA() throws IOException{
		InputStream in = Main.class.getResourceAsStream(className + ".class");
	    ClassReader classReader = new ClassReader(in);
	    ClassNodeVisitor classNode = new ClassNodeVisitor();

	    classReader.accept(classNode, 0);
	    in.close();
	    
	    // Now that we have the instruction list of the method to mutate in hand,
	    // and we know which line to mutate,
	    // go to that line and replace the instruction list with ours
	    
	    for(int i = 0; i < methodList.size(); i++){
	    	if(methodList.get(i).name.equals(methodName)){
	    		updateInsn(methodList.get(i).instructions);
	    		   		
	    		methodList.get(i).instructions = updatedInsn;
	    	}
	    	
	    	classNode.methods.add(methodList.get(i));
	    }
	    
	    // Write the modified class to file
	    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
	    classNode.accept(cw);
	    
	    File outDir = new File("out/RNNA_" + lineToMutate + "/");//        for(int i = 0; i < cv.methodJoin.size(); i++){
////	RTXCMutation rtxc = new RTXCMutation(className, cv.methodJoin.get(i).outerMethod, cv.methodJoin.get(i).lineNumber);
////	rtxc.RTXC();
//	
//	MXTMutation mxt = new MXTMutation(className, cv.methodJoin.get(i).outerMethod, cv.methodJoin.get(i).lineNumber);
//	mxt.MXT();
//	
////	RJSMutation rjs = new RJSMutation(className, cv.methodJoin.get(i).outerMethod, cv.methodJoin.get(i).lineNumber);
////	rjs.RJS();
//}
	    outDir.mkdirs();
	    DataOutputStream dout = new DataOutputStream(new FileOutputStream(new File(outDir, className + ".class")));
	    dout.write(cw.toByteArray());
	    dout.flush();
	    dout.close();
	}

	public void updateInsn(InsnList insn){
		updatedInsn.clear();
		
		InsnList startInsn = new InsnList();
		
		int lineNumber = 0;
		
		for(int i = 0; i < insn.size(); i++){
			AbstractInsnNode currentInsn = insn.get(i);

			if(currentInsn.getType() == 15){
				LineNumberNode lnn = (LineNumberNode) currentInsn;
				lineNumber = lnn.line;
				
				if(lnn.line == lineToMutate){
					// LineNumberNode
					updatedInsn.add(currentInsn);
					// VarInsnNode
					updatedInsn.add(insn.get(i + 1));
					// MethodInsnNode
					updatedInsn.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "notifyAll", "()V", false));
					i += 2;
					continue;
				}
			}
			
			updatedInsn.add(currentInsn);
		}
	}
}
