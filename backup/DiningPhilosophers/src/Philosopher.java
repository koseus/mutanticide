class Philosopher implements Runnable {
    int id = 0;
    Resource r = null;
    int eatCount = 0;
    static int totalEat = 0;
    boolean satisfied = false;
    
    public Philosopher(int initId, Resource initr) {
        id = initId;
        r = initr;
        new Thread(this).start();
    }
    public void run() {
        while (true) {
            try {
//                System.out.println("Phil " + id + " thinking");
                Thread.sleep(30);
                
//                System.out.println("Phil " + id + " hungry");
                r.acquire(id);
                
//                System.out.println("Phil " + id + " eating");
                
                ++eatCount;
                ++totalEat;
                Thread.sleep(40);
                r.release(id);
                if(this.eatCount == 10){
	            		satisfied = true;
	            		break;
	            }
                
                
                
                
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
