package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

import java.util.Queue;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
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
	 * @param	transferPriority	<tt>true</tt> if this queue should
	 *					transfer priority from waiting threads
	 *					to the owning thread.
	 * @return	a new priority thread queue.
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

		Lib.assertTrue(priority >= priorityMinimum &&
				priority <= priorityMaximum);

		getThreadState(thread).setPriority(priority);
	}

	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMaximum)
			return false;

		setPriority(thread, priority+1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMinimum)
			return false;

		setPriority(thread, priority-1);

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
	 * @param	thread	the thread whose scheduling state to return.
	 * @return	the scheduling state of the specified thread.
	 */
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	}

	/**
	 * A <tt>ThreadQueue</tt> that sorts threads by priority.
	 */
	protected class PriorityQueue extends ThreadQueue {
		PriorityQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			//getThreadState(thread).waitForAccess(this);
			ThreadState state = getThreadState(thread);
			state.setStartTime(Machine.timer().getTime());
			//System.out.println("GG:"+waitQueue.contains(state));
			waitQueue.add(state);

			state.waitForAccess(this);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			//getThreadState(thread).acquire(this);
			ThreadState state = getThreadState(thread);
			owner = state;

			state.acquire(this);
		}

		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// implement me

			if (owner != null)
			{
				owner.removeAssociatedList(this);
				owner = null;
			}

			ThreadState thread = waitQueue.peek();
			if (thread != null)
			{
				waitQueue.poll();
				//System.out.println("QQ:"+(thread==waitQueue.poll()));
				//System.out.println(thread+" "+waitQueue.peek());
				thread.waitingQueue = null;
				owner = thread;
				thread.acquire(this);
			}
			else return null;

			return thread.thread;
		}

		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 *
		 * @return	the next thread that <tt>nextThread()</tt> would
		 *		return.
		 */
		protected ThreadState pickNextThread() {
			ThreadState thread = waitQueue.peek();
			
			return thread;
			//return null;
		}

		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// implement me (if you want)
		}

		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
		public boolean transferPriority;

		Comparator<ThreadState> orderer = new Comparator<ThreadState>() {
			public int compare(ThreadState a,ThreadState b)
			{
				int p1 = a.getEffectivePriority();
				int p2 = a.getEffectivePriority();
				if (p1>p2) return -1;
				if (p1<p2) return 1;
				long t1 = a.getStartTime();
				long t2 = b.getStartTime();
				if (t1<t2) return -1;
				if (t1>t2) return 1;
				return 0;
			}
		};

		public Queue<ThreadState> waitQueue = new java.util.PriorityQueue<ThreadState>(100,orderer);

		public ThreadState owner = null;
	}

	/**
	 * The scheduling state of a thread. This should include the thread's
	 * priority, its effective priority, any objects it owns, and the queue
	 * it's waiting for, if any.
	 *
	 * @see	nachos.threads.KThread#schedulingState
	 */
	protected class ThreadState {
		@Override
		public String toString()
		{
			return thread+" "+priority+" "+startTime;
		}
	
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 *
		 * @param	thread	the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
			this.thread = thread;

			priority = -1;
			setPriority(priorityDefault);
		}

		/**
		 * Return the priority of the associated thread.
		 *
		 * @return	the priority of the associated thread.
		 */
		public int getPriority() {
			return priority;
		}

		/**
		 * Return the effective priority of the associated thread.
		 *
		 * @return	the effective priority of the associated thread.
		 */
		public int getEffectivePriority() {
			//return priority;
			return effectivePriority;
		}

		/**
		 * Set the priority of the associated thread to the specified value.
		 *
		 * @param	priority	the new priority.
		 */
		public void setPriority(int priority) {
			if (this.priority == priority)
				return;

			this.priority = priority;

			// implement me
			updateEffectivePriority();
		}

		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the
		 * resource guarded by <tt>waitQueue</tt>. This method is only called
		 * if the associated thread cannot immediately obtain access.
		 *
		 * @param	waitQueue	the queue that the associated thread is
		 *				now waiting on.
		 *
		 * @see	nachos.threads.ThreadQueue#waitForAccess
		 */
		public void waitForAccess(PriorityQueue waitQueue) {
			// implement me
			waitingQueue = waitQueue;
			if (waitQueue.owner != null && waitQueue.transferPriority) waitingQueue.owner.updateEffectivePriority();
		}

		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 *
		 * @see	nachos.threads.ThreadQueue#acquire
		 * @see	nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(PriorityQueue waitQueue) {
			// implement me
			associatedList.add(waitQueue);
			updateEffectivePriority();
		}	

		public void removeAssociatedList(PriorityQueue queue)
		{
			//System.out.println("FFF:"+associatedList.contains(queue));
			associatedList.remove(queue);
			updateEffectivePriority();
		}

		public void updatingEffectivePriority()
		{
			//boolean status = Machine.interrupt().disable();
			//if (nowTurn == turn) return;

			nowTurn = turn;

			int result = effectivePriority;
			effectivePriority = this.getPriority();
			
			if (waitingQueue != null) waitingQueue.waitQueue.remove(this);

			for (int a=0;a<associatedList.size();a++)
			{
				PriorityQueue heap = associatedList.get(a);
				if (heap.transferPriority)
				{
					Iterator<ThreadState> it = heap.waitQueue.iterator();
					while (it.hasNext())
					{
						ThreadState nowState = it.next();
						int priority = nowState.getEffectivePriority();
						if (priority > effectivePriority) effectivePriority = priority;
					}
				}
			}

			if (waitingQueue != null)
			{
				//System.out.println("WMT:"+(waitingQueue.owner == this));
				waitingQueue.waitQueue.add(this);
				if (waitingQueue.owner != null && waitingQueue.transferPriority && effectivePriority != result) waitingQueue.owner.updatingEffectivePriority();
			}

			//Machine.interrupt().restore(status);
		}

		public void updateEffectivePriority()
		{
			boolean status = Machine.interrupt().disable();

			turn ++;
			this.updatingEffectivePriority();

			Machine.interrupt().restore(status);
		}

		/** The thread with which this object is associated. */	   
		protected KThread thread;
		/** The priority of the associated thread. */
		protected int priority;

		private long startTime;

		public long getStartTime()
		{
			return startTime;
		}

		void setStartTime(long startTime)
		{
			this.startTime = startTime;
		}

		public int effectivePriority = priorityMinimum;

		public int nowTurn = 0;

		public LinkedList<PriorityQueue> associatedList = new LinkedList<PriorityQueue>();

		public PriorityQueue waitingQueue = null;
	}
	
	public static int turn = 0;
}
