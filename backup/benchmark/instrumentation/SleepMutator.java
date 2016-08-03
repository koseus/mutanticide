import java.util.*;

public aspect SleepMutator extends Thread{
    pointcut noiseVictem(long i):
	call(void sleep(long)) && args(i) && within(!SleepMutator);

    private static Random rand = new Random();

    void around(long i): noiseVictem(i) {

	try{
	    long newSleepTime = rand.nextInt((int)i*3);// [0,3*sleep]	    
	    if (rand.nextInt(5) == 1)
		proceed(newSleepTime);
	} catch (Exception e) {}	
    }    
};
