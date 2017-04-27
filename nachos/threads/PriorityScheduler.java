//yhdxt`oi`offt`of{inofinofmhphofx`ofxholhofuh`ov`ofphorih
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads based on their priorities.
 * 
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the thread
 * that has been waiting longest.
 * 
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has the
 * potential to starve a thread if there's always a thread waiting with higher
 * priority.
 * 
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
	/**
	 * Allocate a new priority scheduler.
	 */
	public PriorityScheduler() {
	}

	/**
	 * Allocate a new priority thread queue.
	 * 
	 * @param transferPriority
	 *            <tt>true</tt> if this queue should transfer priority from
	 *            waiting threads to the owning thread.
	 * @return a new priority thread queue.
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new PriorityQueue(transferPriority);
	}

	public int getPriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getPriority();
	}

	public int getEffectivePriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getEffectivePriority();
	}

	public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());

		Lib.assertTrue(priority >= priorityMinimum
				&& priority <= priorityMaximum);

		getThreadState(thread).setPriority(priority);
	}

	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMaximum)
			return false;

		setPriority(thread, priority + 1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMinimum)
			return false;

		setPriority(thread, priority - 1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	/**
	 * The default priority for a new thread. Do not change this value.
	 */
	public static final int priorityDefault = 1;
	/**
	 * The minimum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMinimum = 0;
	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = 7;

	/**
	 * Return the scheduling state of the specified thread.
	 * 
	 * @param thread
	 *            the thread whose scheduling state to return.
	 * @return the scheduling state of the specified thread.
	 */
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	}

	/**
	 * A <tt>ThreadQueue</tt> that sorts threads by priority.
	 */
	protected class PriorityQueue extends ThreadQueue implements
			Comparable<PriorityQueue> {
		PriorityQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());

			getThreadState(thread).waitForAccess(this, enqueueTimeCounter++);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());

			if (!transferPriority)
				return;

			getThreadState(thread).acquire(this);
			occupyingThread = thread;
		}

		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());

			//print();

			ThreadState nextThread = pickNextThread();

			if (occupyingThread != null) {
				getThreadState(occupyingThread).release(this);
				occupyingThread = null;
			}

			if (nextThread == null)
				return null;

			waitingQueue.remove(nextThread);
			nextThread.ready();

			updateDonatingPriority();

			acquire(nextThread.getThread());

			return nextThread.getThread();
		}

		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 * 
		 * @return the next thread that <tt>nextThread()</tt> would return.
		 */
		protected ThreadState pickNextThread() {
			if (!waitingQueue.isEmpty())
				return waitingQueue.first();

			return null;
		}

		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());

			for (Iterator<ThreadState> iterator = waitingQueue.iterator(); iterator
					.hasNext();) {
				ThreadState state = iterator.next();
				System.out.print(state.getThread());
			}
			System.out.println();
		}

		public int getDonatingPriority() {
			return donatingPriority;
		}

		public int compareTo(PriorityQueue queue) {
			if (donatingPriority > queue.donatingPriority)
				return -1;
			if (donatingPriority < queue.donatingPriority)
				return 1;

			if (id < queue.id)
				return -1;
			if (id > queue.id)
				return 1;

			return 0;
		}

		public void prepareToUpdateEffectivePriority(KThread thread) {
			boolean success = waitingQueue.remove(getThreadState(thread));

			Lib.assertTrue(success);
		}

		public void updateEffectivePriority(KThread thread) {
			waitingQueue.add(getThreadState(thread));

			updateDonatingPriority();
		}

		protected void updateDonatingPriority() {
			int newDonatingPriority;

			if (waitingQueue.isEmpty())
				newDonatingPriority = priorityMinimum;
			else if (transferPriority)
				newDonatingPriority = waitingQueue.first()
						.getEffectivePriority();
			else
				newDonatingPriority = priorityMinimum;

			if (newDonatingPriority == donatingPriority)
				return;

			if (occupyingThread != null)
				getThreadState(occupyingThread)
						.prepareToUpdateDonatingPriority(this);

			donatingPriority = newDonatingPriority;

			if (occupyingThread != null)
				getThreadState(occupyingThread).updateDonatingPriority(this);
		}

		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
		public boolean transferPriority;

		/** The threads waiting in this ThreadQueue. */
		protected TreeSet<ThreadState> waitingQueue = new TreeSet<ThreadState>();

		/** The thread occupying this ThreadQueue. */
		protected KThread occupyingThread = null;

		protected int donatingPriority = priorityMinimum;

		/**
		 * The number that <tt>waitForAccess</tt> has been called. Used know the
		 * time when each thread enqueue.
		 */
		protected long enqueueTimeCounter = 0;

		protected int id = numPriorityQueueCreated++;
	}

	protected static int numPriorityQueueCreated = 0;

	/**
	 * The scheduling state of a thread. This should include the thread's
	 * priority, its effective priority, any objects it owns, and the queue it's
	 * waiting for, if any.
	 * 
	 * @see nachos.threads.KThread#schedulingState
	 */
	protected class ThreadState implements Comparable<ThreadState> {
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 * 
		 * @param thread
		 *            the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
			this.thread = thread;
		}

		public KThread getThread() {
			return thread;
		}

		/**
		 * Return the priority of the associated thread.
		 * 
		 * @return the priority of the associated thread.
		 */
		public int getPriority() {
			return priority;
		}

		/**
		 * Return the effective priority of the associated thread.
		 * 
		 * @return the effective priority of the associated thread.
		 */
		public int getEffectivePriority() {
			return effectivePriority;
		}

		/**
		 * Return the time when the associated thread begin to wait.
		 */
		public long getEnqueueTime() {
			return enqueueTime;
		}

		/**
		 * Set the priority of the associated thread to the specified value.
		 * 
		 * @param priority
		 *            the new priority.
		 */
		public void setPriority(int priority) {
			if (this.priority == priority)
				return;

			this.priority = priority;
			updateEffectivePriority();
		}

		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the resource
		 * guarded by <tt>waitQueue</tt>. This method is only called if the
		 * associated thread cannot immediately obtain access.
		 * 
		 * @param waitQueue
		 *            the queue that the associated thread is now waiting on.
		 * 
		 * @param enqueueTime
		 *            the time when the thread begin to wait.
		 * 
		 * @see nachos.threads.ThreadQueue#waitForAccess
		 */
		public void waitForAccess(PriorityQueue waitQueue, long enqueueTime) {
			this.enqueueTime = enqueueTime;

			waitingFor = waitQueue;

			waitQueue.updateEffectivePriority(thread);
		}

		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 * 
		 * @see nachos.threads.ThreadQueue#acquire
		 * @see nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(PriorityQueue waitQueue) {
			acquires.add(waitQueue);

			updateEffectivePriority();
		}

		/**
		 * Called when <tt>waitQueue</tt> no longer be acquired by the
		 * associated thread.
		 * 
		 * @param waitQueue
		 *            the queue
		 */
		public void release(PriorityQueue waitQueue) {
			acquires.remove(waitQueue);

			updateEffectivePriority();
		}

		public void ready() {
			Lib.assertTrue(waitingFor != null);

			waitingFor = null;
		}

		public int compareTo(ThreadState state) {

			if (effectivePriority > state.effectivePriority)
				return -1;
			if (effectivePriority < state.effectivePriority)
				return 1;

			if (enqueueTime < state.enqueueTime)
				return -1;
			if (enqueueTime > state.enqueueTime)
				return 1;

			return thread.compareTo(state.thread);
		}

		/**
		 * Remove <tt>waitQueue</tt> from <tt>acquires</tt> to prepare to update
		 * <tt>donatingPriority</tt> of <tt>waitQueue</tt>.
		 * 
		 * @param waitQueue
		 */
		public void prepareToUpdateDonatingPriority(PriorityQueue waitQueue) {
			boolean success = acquires.remove(waitQueue);

			Lib.assertTrue(success);
		}

		public void updateDonatingPriority(PriorityQueue waitQueue) {
			acquires.add(waitQueue);

			updateEffectivePriority();
		}

		private void updateEffectivePriority() {
			int newEffectivePriority = priority;
			if (!acquires.isEmpty())
				newEffectivePriority = Math.max(priority, acquires.first()
						.getDonatingPriority());

			if (newEffectivePriority == effectivePriority)
				return;

			if (waitingFor != null)
				waitingFor.prepareToUpdateEffectivePriority(thread);

			effectivePriority = newEffectivePriority;

			if (waitingFor != null)
				waitingFor.updateEffectivePriority(thread);
		}

		/** The thread with which this object is associated. */
		protected KThread thread;
		/** The priority of the associated thread. */
		protected int priority = priorityDefault;
		/** The effective priority of the associated thread. */
		protected int effectivePriority = priorityDefault;
		/** The ThreadQueue that the associated thread waiting for. */
		protected PriorityQueue waitingFor = null;
		/** The TreeMap storing the number of donated priorities. */
		protected TreeSet<PriorityQueue> acquires = new TreeSet<PriorityQueue>();
		/**
		 * The time when the thread begin to wait. That time is measured by
		 * counting how many times <tt>PriorityQueue.waitForAccess</tt> called
		 * before.
		 */
		protected long enqueueTime;
	}

	private static void test1() {
		System.out.println("PriorityScheduler.test1() begins.");
		KThread thread0 = new KThread(new PSTest(0));
		KThread thread1 = new KThread(new PSTest(1));

		thread0.fork();
		thread1.fork();

		thread0.join();
		thread1.join();
		System.out.println("PriorityScheduler.test1() ends.");
	}

	private static void test2() {
		System.out.println("PriorityScheduler.test2() begins.");
		KThread thread0 = new KThread(new PSTest(0));
		KThread thread1 = new KThread(new PSTest(1));

		boolean intStatus = Machine.interrupt().disable();

		ThreadedKernel.scheduler.setPriority(thread0, 2);

		Machine.interrupt().restore(intStatus);

		thread0.fork();
		thread1.fork();

		thread0.join();
		thread1.join();
		System.out.println("PriorityScheduler.test2() ends.");
	}

	private static void test3() {
		System.out.println("PriorityScheduler.test3() begins.");

		KThread thread0 = new KThread(new PSTest(0));
		KThread thread1 = new KThread(new PSTest(1));

		thread0.setName("thread0");
		thread1.setName("thread1");

		boolean intStatus = Machine.interrupt().disable();

		ThreadedKernel.scheduler.setPriority(thread0, 2);

		Machine.interrupt().restore(intStatus);

		thread0.fork();
		thread1.fork();

		/*
		 * The main thread which has increased its priority is waiting for
		 * thread1.
		 */
		ThreadedKernel.scheduler.increasePriority();
		thread1.join();
		thread0.join();
		ThreadedKernel.scheduler.decreasePriority();

		System.out.println("PriorityScheduler.test3() ends.");
	}
	
	private static void test4() {
		System.out.println("PriorityScheduler.test4() begins.");

		KThread thread0 = new KThread(new PSTest(0));
		KThread thread1 = new KThread(new PSTest(1));

		thread0.setName("thread0");
		thread1.setName("thread1");

		boolean intStatus = Machine.interrupt().disable();

		ThreadedKernel.scheduler.setPriority(thread0, 2);

		Machine.interrupt().restore(intStatus);

		thread0.fork();
		thread1.fork();

		/*
		 * The main thread which has increased its priority is waiting for
		 * thread1.
		 */
		ThreadedKernel.scheduler.increasePriority();
		ThreadedKernel.scheduler.increasePriority();
		thread1.join();
		thread0.join();
		ThreadedKernel.scheduler.decreasePriority();
		ThreadedKernel.scheduler.decreasePriority();

		System.out.println("PriorityScheduler.test4() ends.");
	}
	
	private static void test5() {
		System.out.println("PriorityScheduler.test5() begins.");

		KThread thread0 = new KThread(new PSTest(0));
		KThread thread1 = new KThread(new PSTest(1));
		KThread thread2 = new KThread(new PSTest(2));

		thread0.setName("thread0");
		thread1.setName("thread1");
		thread2.setName("thread2");

		/* Disable interrupt for convenience. */
		boolean intStatus = Machine.interrupt().disable();

		ThreadedKernel.scheduler.setPriority(thread0, 2);
		ThreadedKernel.scheduler.setPriority(thread1, 2);
		ThreadedKernel.scheduler.setPriority(thread2, 1);

		thread0.fork();
		thread1.fork();
		thread2.fork();
		
		/* Priority of thread1 is changed later than thread2, but that thread is enqueued earlier. */
		ThreadedKernel.scheduler.setPriority(thread1, 1);

		/* Wait until all the threads ends. */
		thread0.join();
		thread1.join();
		thread2.join();
		
		Machine.interrupt().restore(intStatus);

		System.out.println("PriorityScheduler.test5() ends.");
	}
	private static class Competitor implements Runnable {
		private int index;
		private Communicator pubCom;
		private Communicator priCom;
		
		Competitor(int _index, Communicator _pubCom, Communicator _priCom) {
			index = _index;
			pubCom = _pubCom;
			priCom = _priCom;
		}
		
		public void run() {
			if(pubCom.listen() > 0)
				priCom.speak(index);
		}
	}
	private static class Referee implements Runnable {
		private int capacity;
		private Communicator pubCom;
		private Communicator priCom;
		
		Referee(int _capacity, Communicator _pubCom, Communicator _priCom){
			capacity = _capacity;
			pubCom = _pubCom;
			priCom = _priCom;
		}
		
		public void run() {
			while(capacity>0){
				pubCom.speak(capacity);
				int temp = priCom.listen();
				for(int i = 0; i < 5; i++){
					System.out.println("Competitor " + temp + " loop " + i);
				}
				capacity--;
			}
		}
	}
	private static void test6() {
		Communicator pubCom = new Communicator();
		Communicator priCom = new Communicator();
		int capacity = 7;
		KThread[] competitors = new KThread[10];
		for(int i = 0; i < capacity; i++){
			competitors[i] = new KThread(new Competitor(i, pubCom, priCom));
		}
		KThread referee = new KThread(new Referee(capacity, pubCom, priCom));
		boolean intStatus = Machine.interrupt().disable();
		for(int i = 0; i < capacity; i++){
			int priority = (int)(Math.random()*7);
			ThreadedKernel.scheduler.setPriority(competitors[i], priority);
			System.out.println("Competitor " + i + " with priority " + priority);
		}
		for(int i = 0; i < capacity; i++)
			competitors[i].fork();
		referee.fork();
		for(int i = 0; i < capacity; i++)
			competitors[i].join();
		referee.join();
		Machine.interrupt().restore(intStatus);
	}
	private static class Joiner implements Runnable {
		int length;
		String name;
		Joiner(int _length, String _name){
			length = _length;
			name = _name;
		}
		public void run(){
			for(int i = 0; i < length; i++)
				System.out.println(name + " loop " + i);
		}
	}
	private static class Joinee implements Runnable {
		private KThread joiner;
		int length;
		String name;
		Joinee(int _length, String _name, KThread _joiner){
			length = _length;
			joiner = _joiner;
			name = _name;
		}
		public void run(){
			joiner.join();
			for(int i = 0; i < length; i++)
				System.out.println(name + " loop " + i);
		}
	}
	private static void test7() {
		KThread threadO = new KThread(new Joiner(2, "threadO"));
		KThread threadL = new KThread(new Joinee(2, "threadL", threadO));
		KThread threadM = new KThread(new Joinee(10, "threadM", threadO));
		KThread threadH = new KThread(new Joinee(2, "threadH", threadL));
		
		threadH.setName("threadH");
		threadM.setName("threadM");
		threadL.setName("threadL");
		threadO.setName("threadO");

		boolean intStatus = Machine.interrupt().disable();
		
		ThreadedKernel.scheduler.setPriority(threadO, 7);
		ThreadedKernel.scheduler.setPriority(threadH, 7);
		ThreadedKernel.scheduler.setPriority(threadM, 4);
		ThreadedKernel.scheduler.setPriority(threadL, 1);
		
		threadM.fork();
		threadL.fork();
		threadH.fork();
		threadO.fork();
		threadO.join();
		threadM.join();
		threadL.join();
		threadH.join();
		
		
		Machine.interrupt().restore(intStatus);
	}
	
	public static void selfTest() {
		test1();
		test2();
		test3();
		test4();
		test5();
		test6();
		test7();
	}

	private static class PSTest implements Runnable {
		private int index;

		PSTest(int _index) {
			index = _index;
		}

		public void run() {
			int i;
			boolean intStatus = Machine.interrupt().disable();
			System.out.println("EffectivePriority of thread" + index + " is "
					+ ThreadedKernel.scheduler.getEffectivePriority());
			Machine.interrupt().restore(intStatus);
			for (i = 0; i < 5; i++) {
				System.out.println("PSTest " + index + " loop " + i);
				KThread.yield();
			}
		}
	}
}
