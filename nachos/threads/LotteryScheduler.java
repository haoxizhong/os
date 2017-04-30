//yhdxt`oi`offt`of{inofinofmhphofx`ofxholhofuh`ov`ofphorih
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

import java.util.LinkedList;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
	/**
	 * Allocate a new lottery scheduler.
	 */
	public LotteryScheduler() {
	}

	/**
	 * Allocate a new lottery thread queue.
	 *
	 * @param	transferPriority	<tt>true</tt> if this queue should
	 *					transfer tickets from waiting threads
	 *					to the owning thread.
	 * @return	a new lottery thread queue.
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new LotteryQueue(transferPriority);
	}

	protected zhxThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new zhxThreadState(thread);

		return (zhxThreadState) thread.schedulingState;
	}

	class LotteryQueue extends PriorityScheduler.PriorityQueue {
		LotteryQueue(boolean transferPriority) {
			super(transferPriority);
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

			zhxThreadState nextThread = pickNextThread();

			if (occupyingThread != null) {
				getThreadState(occupyingThread).release(this);
				occupyingThread = null;
			}

			if (nextThread == null)
				return null;

			waitingList.remove(nextThread);
			nextThread.ready();

			updateDonatingPriority();

			acquire(nextThread.getThread());

			return nextThread.getThread();
		}

		protected zhxThreadState pickNextThread() {
			if (!waitingList.isEmpty()) {
				int total = 0;
				for (int a=0;a<waitingList.size();a++)
					total += waitingList.get(a).getEffectivePriority();
				int rand = Lib.random(total);
				int sum = 0;
				for (int a=0;a<waitingList.size();a++)
				{
					sum += waitingList.get(a).getEffectivePriority();
					if (sum>=rand) return waitingList.get(a);
				}
			}

			return null;
		}

		LinkedList<KThread> updating = new LinkedList<KThread>();

		public void prepareToUpdateEffectivePriority(KThread thread) {
			if (updating.contains(thread)) return;
			boolean success = waitingList.remove(getThreadState(thread));
			//Lib.assertTrue(success);
		}

		public void updateEffectivePriority(KThread thread) {
			if (updating.contains(thread)) return;
			updating.add(thread);
			waitingList.add(getThreadState(thread));

			updateDonatingPriority();
			updating.remove(thread);
		}

		protected void updateDonatingPriority() {
			int newDonatingPriority;

			if (waitingList.isEmpty())
				newDonatingPriority = priorityMinimum;
			else if (transferPriority)
			{
				newDonatingPriority = 0;
				for (int a=0;a<waitingList.size();a++)
					newDonatingPriority += waitingList.get(a).getEffectivePriority();
			}
			else newDonatingPriority = priorityMinimum;

			if (newDonatingPriority == donatingPriority) return;

			if (occupyingThread != null) getThreadState(occupyingThread).prepareToUpdateEffectivePriority(this);

			donatingPriority = newDonatingPriority;
			
			if (occupyingThread != null) getThreadState(occupyingThread).updateDonatingPriority(this);
		}

		protected LinkedList<zhxThreadState> waitingList = new LinkedList<zhxThreadState>();
	}

	class zhxThreadState extends PriorityScheduler.ThreadState {
		public zhxThreadState(KThread thread) {
			super(thread);
		}
		public void setPriority(int priority) {
			if (this.priority == priority) return;
			this.priority = priority;
			updateEffectivePriority();
		}
		public void waitForAccess(LotteryQueue waitList,long enqueueTime) {
			this.enqueueTime = enqueueTime;

			waitingForList = waitList;

			waitList.updateEffectivePriority(thread);
		}

		public void acquire(LotteryQueue waitList) {
			acquires.add(waitList);

			updateEffectivePriority();
		}

		public void release(LotteryQueue waitList) {
			acquires.remove(waitList);

			updateEffectivePriority();
		}

		public void ready() {
			Lib.assertTrue(waitingForList != null);
			waitingForList = null;
		}

		public void prepareToUpdateEffectivePriority(LotteryQueue waitList) {
			boolean success = acquires.remove(waitList);
			//Lib.assertTrue(success);
		}

		public void updateDonatingPriority(LotteryQueue waitList) {
			acquires.add(waitList);
			updateEffectivePriority();
		}

		public void updateEffectivePriority() {
			int newEffectivePriority = priority;

			if (!acquires.isEmpty())
			{
				for (int a=0;a<acquires.size();a++)
					newEffectivePriority += acquires.get(a).getDonatingPriority();
			}

			if (newEffectivePriority == effectivePriority) return;

			if (waitingForList != null) waitingForList.prepareToUpdateEffectivePriority(thread);

			effectivePriority = newEffectivePriority;

			if (waitingForList != null) waitingForList.updateEffectivePriority(thread);

		}
		
		public LinkedList<LotteryQueue> acquires = new LinkedList<LotteryQueue>();

		public LotteryQueue waitingForList = null;
	}
}
