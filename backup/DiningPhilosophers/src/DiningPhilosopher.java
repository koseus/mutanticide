class DiningPhilosopher implements Resource {
    int n = 0;
    BinarySemaphore[] fork = null;
    static Philosopher[] philosophers = new Philosopher[5];
    
    public DiningPhilosopher(int initN) {
        n = initN;
        fork = new BinarySemaphore[n];
        for (int i = 0; i < n; i++) {
            fork[i] = new BinarySemaphore(true);
        }
    }
    
    public void acquire(int i) {
        fork[i].P();
        fork[(i + 1) % n].P();
    }
    
    public void release(int i) {
        fork[i].V();
        fork[(i + 1) % n].V();
    }
    
    public static void main(String[] args) {
    		DiningPhilosopher dp = new DiningPhilosopher(5);
        for (int i = 0; i < 5; i++){
        		philosophers[i] = new Philosopher(i, dp);
        }
        
        while(true){
	    		int satisfiedCount = 0;
	    		
	    		for (int i = 0; i < 5; i++){
	    			if(philosophers[i].satisfied){
	    				++satisfiedCount;
	    			}
	    		}
	    		
	    		if(satisfiedCount == 5){
	    			System.out.print(Philosopher.totalEat);
	    			break;
	    		}
	    	}
    }
}
