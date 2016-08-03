public class BinarySemaphore {
    boolean value;
    BinarySemaphore(boolean initValue) {
        value = initValue;
    }
    public synchronized void P() throws InterruptedException {
        while (value == false){
            wait();// in queue of blocked processes
        }
        value = false;
    }
    public synchronized void V() {
        value = true;
        notify();
    }
}
