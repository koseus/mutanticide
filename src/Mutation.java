import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class Mutation {
	final String className;
	String methodToMutate;
	
	List<MethodNode> methodList = new ArrayList<MethodNode>();
	
	public Mutation(String name, String method){
		className = name;
		methodToMutate = method;
	}
}
