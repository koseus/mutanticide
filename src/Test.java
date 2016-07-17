
public class Test extends Thread{

	volatile int x = 1;
	static int count;
	
	public void run(){
		++count;
		
		System.out.println("Statement 1" + this.getName());
		
		synchronized (this) {
			System.out.println("Statement 2" + this.getName());
			System.out.println("Statement 3" + this.getName());
			System.out.println("Statement 4" + this.getName());
			System.out.println("Statement 5" + this.getName());
		}
		
		System.out.println("Statement 6" + this.getName());
		
//		myMethod();
	}
	
	synchronized void myMethod(){
		long time = 5000;
		try {
			wait(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("End");
	}
	
	public static void main(String[] args) throws InterruptedException {
		long time = 5000;
		
		Test.count = 0;
		
		Test test1 = new Test();
		Test test2 = new Test();
		Test test3 = new Test();
		
		test1.start();
		
		
//		try {
//			test1.wait(time);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		test2.start();
		
//		try {
//			test2.wait(time);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		
		test3.start();
		
	}
	
	public void x(){
		int x = 5;
		
		x = x - 5;
		
	}

}
