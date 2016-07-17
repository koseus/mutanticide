import org.objectweb.asm.tree.InsnList;

public class CRMutation extends Mutation {
	final int lineToMutate;
	
	// List to hold the instructions of the method
	InsnList insnList = new InsnList();
	
	InsnList updatedInsnList = new InsnList();
	
	public CRMutation(String name, String method, int line){
		super(name, method);
		
		lineToMutate = line;
	}
}
