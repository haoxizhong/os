//yhdxt`oi`offt`of{inofinofmhphofx`ofxholhofuh`ov`ofphorih
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 * 
 * <p>
 * You must implement this.
 * 
 * @see nachos.threads.Condition
 */
public class Condition2 {
	/**
	 * Allocate a new condition variable.
	 * 
	 * @param conditionLock
	 *            the lock associated with this condition variable. The current
	 *            thread must hold this lock whenever it uses <tt>sleep()</tt>,
	 *            <tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock;
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically
	 * reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		boolean intStatus = Machine.interrupt().disable();

		waitQueue.waitForAccess(KThread.currentThread());

		conditionLock.release();

		KThread.sleep();

		conditionLock.acquire();

		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		boolean intStatus = Machine.interrupt().disable();

		KThread nextThread = waitQueue.nextThread();

		if (nextThread != null)
			nextThread.ready();

		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		boolean intStatus = Machine.interrupt().disable();

		KThread nextThread;

		while ((nextThread = waitQueue.nextThread()) != null)
			nextThread.ready();

		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Test tools for Condition2
	 */
	private static class Int {
		int value;
		Int(int _value){
			value = _value;
		}
		public void inc(){
			value++;
		}
		public void dec(){
			value--;
		}
		public int val(){
			return value;
		}
	}
	private static class Producer implements Runnable {
		private Int goods;
		private Condition2 condition;
		private Lock lock;
		private int index;
		
		Producer(int _index, Int _goods, Condition2 _condition, Lock _lock){
			index = _index;
			goods = _goods;
			condition = _condition;
			lock = _lock;
		}
		
		public void run() {
			lock.acquire();
			System.out.println("Producer " + index + " starts running");
			goods.inc();
			System.out.println("Producer " + index + " produces 1 item (" + goods.val() + " items)");
			condition.wakeAll();
			System.out.println("Producer " + index + " ends running");
			lock.release();
		}
	}
	private static class Consumer implements Runnable {
		private Int goods;
		private Condition2 condition;
		private Lock lock;
		private int index;
		
		Consumer(int _index, Int _goods, Condition2 _condition, Lock _lock) {
			index = _index;
			goods = _goods;
			condition = _condition;
			lock = _lock;
		}
		
		public void run() {
			lock.acquire();
			System.out.println("Consumer " + index + " starts running");
			while(goods.val() < 1){
				System.out.println("Consumer " + index + " sleeps " + "(" + goods.val() + " items)");
				condition.sleep();
			}
			goods.dec();
			System.out.println("Consumer " + index + " consumes " + "" + 1 + " item (" + goods.val() + " items)");
			System.out.println("Consumer " + index + " ends running");
			lock.release();
		}
	}
	
	public static void selfTest(){
		Int goods = new Int(0);
		Lock lock = new Lock();
		Condition2 condition = new Condition2(lock);
		KThread consumer1 = new KThread(new Consumer(1, goods, condition, lock));
		KThread consumer2 = new KThread(new Consumer(2, goods, condition, lock));
		KThread producer1 = new KThread(new Producer(1, goods, condition, lock));
		KThread producer2 = new KThread(new Producer(2, goods, condition, lock));
		KThread producer3 = new KThread(new Producer(3, goods, condition, lock));
		KThread consumer3 = new KThread(new Consumer(3, goods, condition, lock));
		consumer1.fork();
		consumer2.fork();
		producer1.fork();
		producer2.fork();
		producer3.fork();
		consumer3.fork();
		ThreadedKernel.alarm.waitUntil(100000);
	}
	
	private Lock conditionLock;
	private ThreadQueue waitQueue = ThreadedKernel.scheduler
			.newThreadQueue(false);
}
