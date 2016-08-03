public class CountingSemaphore {
    int value;
    public CountingSemaphore(int initValue) {
        value = initValue;
    }
    public synchronized void P() throws InterruptedException {
        value--;
//        System.out.println("buffer capacity: " + value);
        if (value < 0) 
        		wait();
    }
    public synchronized void V() {
        value++;
//        System.out.println("buffer capacity: " +  value);
        if (value <= 0){
        		notify();
        }
    }
}
