import java.util.*;

public aspect SleepNoise extends Thread{
    pointcut noiseVictem():
        ((get(* *) || set (* *))&& within(!SleepNoise));
    private static Random rand = new Random();
    after(): noiseVictem() {
        try{ // noise
	    if (rand.nextInt(100) == 1){ // activation probability
		sleep(rand.nextInt(50)); // noise type
	    }
        } catch (Exception e) {};
    }
}
