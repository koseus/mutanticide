public class BinarySemaphore {
    boolean value;
    BinarySemaphore(boolean initValue) {
        value = initValue;
    }
    public synchronized void P() {
        while (value == false) {
            try {
				wait();
			} catch (InterruptedException e) {
			}// in queue of blocked processes
        }
        value = false;
    }
    public synchronized void V() {
        value = true;
        notify();
    }
}
