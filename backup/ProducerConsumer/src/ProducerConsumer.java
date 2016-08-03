import java.util.Random;
class Producer implements Runnable {
	public static int totalProduced = 0;
    BoundedBuffer b = null;
        
    public Producer(BoundedBuffer initb) {
        b = initb;
        new Thread(this).start();
    }
    public void run() {
        double item;
        Random r = new Random();
        while (true) {
            item = r.nextDouble();
//            System.out.println("produced item " + item);
            try {
				b.deposit(item);
				++totalProduced;
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}

class Consumer implements Runnable {
	public static int totalConsumed = 0;
	public int consumeCount = 0;
	public boolean isSatisfied = false;
	
    BoundedBuffer b = null;
    public Consumer(BoundedBuffer initb) {
        b = initb;
        new Thread(this).start();
    }
    public void run() {
        double item;
        while (true) {
        		if(consumeCount == 5){
        			isSatisfied = true;
        			break;
        		}
        		
            try {
				item = b.fetch();
				++consumeCount;
				++totalConsumed;
//	            System.out.println("fetched item " + item);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
             try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}
class ProducerConsumer {
    static Consumer[] consumers = new Consumer[5];
    
    public static void main(String[] args) {
        BoundedBuffer buffer = new BoundedBuffer();
        Producer producer = new Producer(buffer);
       
        for(int i = 0; i < 5; i++){
        		consumers[i] = new Consumer(buffer);
        }
        
        while(true){
    		int satisfiedCount = 0;
    		
    		for (int i = 0; i < 5; i++){
    			if(consumers[i].isSatisfied){
    				++satisfiedCount;
    			}
    		}
    		
    		if(satisfiedCount == 5){
    			System.out.print(Producer.totalProduced);
    			break;
    		}
    	}
    }
}

