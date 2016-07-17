
import java.util.ArrayList;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class ClassTraverser extends ClassNode{
	final String className;
	
	public static ArrayList<Field> volatileFields = new ArrayList<Field>();

	// To generate RSB, EXCR, SKCR, SHCR, SPCR mutants
	public static ArrayList<SynchronizedBlock> syncBlocks = new ArrayList<SynchronizedBlock>();
	
	// Method invocation lines
	public static ArrayList<MethodXLine> methodJoin = new ArrayList<MethodXLine>();
	public static ArrayList<MethodXLine> methodWait = new ArrayList<MethodXLine>();
	public static ArrayList<MethodXLine> methodSleep = new ArrayList<MethodXLine>();
	public static ArrayList<MethodXLine> methodAwait = new ArrayList<MethodXLine>();
	public static ArrayList<MethodXLine> methodYield = new ArrayList<MethodXLine>();
	public static ArrayList<MethodXLine> methodNotify = new ArrayList<MethodXLine>();
	public static ArrayList<MethodXLine> methodNotifyAll = new ArrayList<MethodXLine>();

	// To generate RSK and and ASK mutants
	public static ArrayList<MethodBeginLine> staticMethods = new ArrayList<MethodBeginLine>();
	public static ArrayList<MethodBeginLine> nonStaticMethods = new ArrayList<MethodBeginLine>();

	// To generate RSTK and ASTK mutants
	public static ArrayList<MethodBeginLine> synchronizedMethods = new ArrayList<MethodBeginLine>();
	public static ArrayList<MethodBeginLine> nonSynchronizedMethods = new ArrayList<MethodBeginLine>();

	public ClassTraverser(String className){
		super(ASM5);
		
		this.className = className;
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
//		System.out.println(name);
//		System.out.println(access);
		
		boolean isStatic = false;
		boolean isSynchronized = false;
		
		String accessString = Integer.toBinaryString(access);
		
		if(accessString.length() >= 4){
			if(accessString.substring(
					accessString.length() - 4, accessString.length() - 3).equals("1")){
				isStatic = true;
			}
		}
		
		if(accessString.length() >= 6){
			if(accessString.substring(
					accessString.length() - 6, accessString.length() - 5).equals("1")){
				isSynchronized = true;
			}
		}
		
		if(isStatic){
			staticMethods.add(new MethodBeginLine(className, name));
		}else{
			nonStaticMethods.add(new MethodBeginLine(className, name));
		}
		
		if(isSynchronized){
			synchronizedMethods.add(new MethodBeginLine(className, name));
		}else{
			nonSynchronizedMethods.add(new MethodBeginLine(className, name));
		}
		
		
		return new MethodExplorer(this.name, name);
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value){

		String accessString = Integer.toBinaryString(access);

		if(accessString.length() >= 7){
			if(accessString.substring(accessString.length() - 7, accessString.length() - 6).equals("1")){

				volatileFields.add(new Field(className, name));
			}
		}
		return super.visitField(access, name, desc, signature, value);
	}


	class MethodExplorer extends MethodNode{
		String className;
		String methodName;

		public MethodExplorer(String className, String name){
			super(ASM5);

			this.className = className;
			methodName = name;
		}
		
		public void visitEnd(){
			int lineNumber = 0;
			
			for(int i = 0; i < this.instructions.size(); i++){
				AbstractInsnNode currentInsn = this.instructions.get(i);
				
				if(currentInsn.getType() == 15){
					LineNumberNode lnn = (LineNumberNode) currentInsn;
					lineNumber = lnn.line;
				}else if(currentInsn.getType() == 0){
					InsnNode in = (InsnNode) currentInsn;
					
					if(in.getOpcode() == Opcodes.MONITORENTER){
						syncBlocks.add(new SynchronizedBlock(className, methodName, lineNumber));
					}
				}else if(currentInsn.getType() == 5){
					MethodInsnNode min = (MethodInsnNode) currentInsn;
					
					switch(min.name){
					case "wait":
						methodWait.add(new MethodXLine(className, methodName, lineNumber));
						break;
					case "await":
						methodAwait.add(new MethodXLine(className, methodName, lineNumber));
						break;
					case "join":
						methodJoin.add(new MethodXLine(className, methodName, lineNumber));
						break;
					case "sleep":
						methodSleep.add(new MethodXLine(className, methodName, lineNumber));
						break;
					case "yield":
						methodYield.add(new MethodXLine(className, methodName, lineNumber));
						break;
					case "notify":
						methodNotify.add(new MethodXLine(className, methodName, lineNumber));
						break;
					case "notifyAll":
						methodNotifyAll.add(new MethodXLine(className, methodName, lineNumber));
						break;
					}
				}
			}
		}
		
		
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index){
//			super.visitLocalVariable(name, desc, signature, start, end, index);
		}
		
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type){
//			super.visitTryCatchBlock(start, end, handler, type);
		}
	}

	class MethodDeclarations extends MethodVisitor{
		String methodName;
		int lineNumber;

		public MethodDeclarations(String name){
			super(ASM5);

			methodName = name;
		}
	}
}

class Field {
	final String className;
	final String fieldName;

	public Field(String className, String name){
		this.className = className;
		fieldName = name;
	}
}


class MethodXLine {
	final String className;
	final String methodName;
	final int lineNumber;

	public MethodXLine(String className, String method, int line){
		this.className = className;
		methodName = method;
		lineNumber = line;
	}
}


class SynchronizedBlock {
	final String className;
	final String methodName;
	final int lineNumber;

	public SynchronizedBlock(String className, String name, int line){
		this.className = className;
		methodName = name;
		lineNumber = line;
	}
}

class MethodBeginLine {
	final String className;
	final String methodName;

	public MethodBeginLine(String name, String method){
		className = name;
		methodName = method;
	}
}
