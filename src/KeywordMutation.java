import static org.objectweb.asm.Opcodes.ASM5;

import org.objectweb.asm.tree.MethodNode;

public class KeywordMutation extends Mutation{
	
	public KeywordMutation(String name, String method){
		super(name, method);
	}
	
	class MethodNodeVisitor extends MethodNode{		
		public MethodNodeVisitor(int access, String name, String desc, String signature, String[] exceptions){
			super(ASM5, access, name, desc, signature, exceptions);
		}
		
		public void visitEnd(){
			methodList.add(this);
		}
	}
}
