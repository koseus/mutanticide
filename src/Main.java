
import java.io.*;

import org.objectweb.asm.*;

public class Main {

    public static void main(String[] args) throws IOException {
		String className = "Simple";

    	InputStream in = Main.class.getResourceAsStream(className + ".class");
        ClassReader classReader = new ClassReader(in);

        // Traverse the class and its methods
        ClassTraverser cv = new ClassTraverser();
        classReader.accept(cv, 0);
        in.close();

        for(int i = 0; i < ClassTraverser.volatileFields.size(); i++){
        	System.out.println(ClassTraverser.volatileFields.get(i).fieldName);

        	RVKMutation rvk = new RVKMutation(className, ClassTraverser.volatileFields.get(i).fieldName);
        	rvk.RVK();
        }
    }
}
