FIFOReadWriteLock is an implementation of the Doug Lea's readers writers lock ensuring a "first in first out" order in java.util.concurrent (release 1.3.4).  This modified version was used in an empirical study on V&V tools/techniques for Concurrent Java Components.

It relies on the following classes:
FIFOSemaphore
QueuedSemaphore
Semaphore
Sync

Source code can be found at http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html

Defects seeded:
1. Deadlock with acquireRead() and attemptWrite() methods
2. Interference
