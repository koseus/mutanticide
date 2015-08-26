
public class Simple {
	int x = 5;
	volatile int y = 2;
	
	public void test(){
		System.out.println(x);
		System.out.println(y);
	}
	
	public static void main(String[] args) {
		Simple simple = new Simple();
		simple.test();
	}

}
