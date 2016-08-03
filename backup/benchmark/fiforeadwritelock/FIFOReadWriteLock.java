/*
  File: FIFOReadWriteLock.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  11Jun1998  dl               Create public version
  23nov2001  dl               Replace main algorithm with fairer
                              version based on one by Alexander Terekhov
  25Aug2005  mw               Modifications
  14Sep2005  mw               Further modifications
*/



/**
 * This class implements a policy for reader/writer locks in which
 * threads contend in a First-in/First-out manner for access (modulo
 * the limitations of FIFOSemaphore, which is used for queuing).  This
 * policy does not particularly favor readers or writers.  As a
 * byproduct of the FIFO policy, the <tt>attempt</tt> methods may
 * return <tt>false</tt> even when the lock might logically be
 * available, but, due to contention, cannot be accessed within the
 * given time bound.  <p>
 *
**/

public class FIFOReadWriteLock {

  /** 
   * Fair Semaphore serving as a kind of mutual exclusion lock.
   * Writers acquire on entry, and hold until rwlock exit.
   * Readers acquire and release only during entry (but are
   * blocked from doing so if there is an active writer).
   **/
  protected final FIFOSemaphore entryLock = new FIFOSemaphore(1);

  /** 
   * Number of threads that have entered read lock.  
   **/
  private int readers;

  /** 
   * Number of threads that have exited read lock. 
   **/
  private int exreaders;

  protected synchronized void acquireRead() throws InterruptedException {
    entryLock.acquire();
    ++readers; 
    entryLock.release();
  }

  protected synchronized void releaseRead() {
    
    if (++exreaders == readers) 
      notify(); 
  }

  protected void acquireWrite() throws InterruptedException {
    
    entryLock.acquire();
    
    int r = readers;
    synchronized(this) {
	while (exreaders != r) 
	    wait();
    }
    
  }

  protected void releaseWrite() {
    entryLock.release();
  }

  protected boolean attemptRead(long msecs) throws InterruptedException {
    if (!entryLock.attempt(msecs)) 
      return false;
    entryLock.release();
    ++readers; 
    return true;
  }

  protected boolean attemptWrite(long msecs) throws InterruptedException {

    if (!entryLock.attempt(msecs)) 
      return false;

    int r = readers;

    synchronized(this) {
        while (exreaders != r) {
          
          wait();
        }
        return true;
    }
    
  }

}

