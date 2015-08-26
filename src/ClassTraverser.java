
import java.util.ArrayList;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class ClassTraverser extends ClassNode{
	public static ArrayList<Field> volatileFields = new ArrayList<Field>();

	public static ArrayList<SynchronizedBlock> syncBlocks = new ArrayList<SynchronizedBlock>();

	public static ArrayList<MethodXLine> methodJoin = new ArrayList<MethodXLine>();
	public static ArrayList<MethodXLine> methodWait = new ArrayList<MethodXLine>();
	public static ArrayList<MethodXLine> methodSleep = new ArrayList<MethodXLine>();
	public static ArrayList<MethodXLine> methodAwait = new ArrayList<MethodXLine>();
	public static ArrayList<MethodXLine> methodYield = new ArrayList<MethodXLine>();
	public static ArrayList<MethodXLine> methodNotify = new ArrayList<MethodXLine>();
	public static ArrayList<MethodXLine> methodNotifyAll = new ArrayList<MethodXLine>();

	public static ArrayList<MethodBeginLine> staticMethods = new ArrayList<MethodBeginLine>();
	public static ArrayList<MethodBeginLine> nonStaticMethods = new ArrayList<MethodBeginLine>();

	public static ArrayList<MethodBeginLine> synchronizedMethods = new ArrayList<MethodBeginLine>();
	public static ArrayList<MethodBeginLine> nonSynchronizedMethods = new ArrayList<MethodBeginLine>();

	public ClassTraverser(){
		super(ASM5);
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
		return new MethodInvocations(name);
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value){

		String accessString = Integer.toBinaryString(access);

		if(accessString.length() >= 7){
			if(accessString.substring(accessString.length() - 7, accessString.length() - 6).equals("1")){
//				System.out.println(name);

				volatileFields.add(new Field(name));
			}
		}


		return super.visitField(access, name, desc, signature, value);
	}


	class MethodInvocations extends MethodVisitor{
		private String methodName;
		private int lineNumber;

		public MethodInvocations(String name){
			super(ASM5);

			methodName = name;
		}

		// record which method is called on which line
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf){
        	if(name.equals("join")){
        		methodJoin.add(new MethodXLine(name, this.lineNumber));
        	}else if(name.equals("wait")){
        		methodWait.add(new MethodXLine(name, this.lineNumber));
        	}else if(name.equals("sleep")){
        		methodSleep.add(new MethodXLine(name, this.lineNumber));
        	}else if(name.equals("await")){
        		methodAwait.add(new MethodXLine(name, this.lineNumber));
        	}else if(name.equals("yield")){
        		methodYield.add(new MethodXLine(name, this.lineNumber));
        	}else if(name.equals("notify")){
        		methodNotify.add(new MethodXLine(name, this.lineNumber));
        	}else if(name.equals("notifyAll")){
        		methodNotifyAll.add(new MethodXLine(name, this.lineNumber));
        	}
        }
	}

	class MethodDeclarations extends MethodVisitor{
		private String methodName;
		private int lineNumber;

		public MethodDeclarations(String name){
			super(ASM5);

			methodName = name;
		}
	}

}

class Field {
	final String fieldName;

	public Field(String name){
		fieldName = name;
	}
}


class MethodXLine {
	private final String methodName;
	private final int lineNumber;

	public MethodXLine(String name, int line){
		methodName = name;
		lineNumber = line;
	}

	public String getMethodName(){
		return methodName;
	}

	public int getLineNumber(){
		return lineNumber;
	}
}


class SynchronizedBlock {
	private final String methodName;
	private final int lineNumber;

	public SynchronizedBlock(String name, int line){
		methodName = name;
		lineNumber = line;
	}

	public String getMethodName(){
		return methodName;
	}

	public int getLineNumber(){
		return lineNumber;
	}
}

class MethodBeginLine {
	private final String methodName;
	private int beginLine;

	public MethodBeginLine(String name, int line){
		methodName = name;
		beginLine = line;
	}

	public String getMethodName(){
		return methodName;
	}

	public int getBeginLine(){
		return beginLine;
	}

	public void setBeginLine(int line){
		beginLine = line;
	}
}
