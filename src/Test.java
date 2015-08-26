
public class Test extends Thread{

	public void run(){
		System.out.println("Statement 1");

		synchronized (this) {
			System.out.println("Statement 2");
			System.out.println("Statement 3");
			System.out.println("Statement 4");
			System.out.println("Statement 5");
		}

		System.out.println("Statement 6");
	}

	public static void main(String[] args) {
		Test test = new Test();

		test.start();
	}

}
