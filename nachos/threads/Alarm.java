package nachos.threads;

import nachos.machine.*;

import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Queue;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 *
	 * <p><b>Note</b>: Nachos will not function correctly with more than one
	 * alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() { timerInterrupt(); }
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread
	 * that should be run.
	 */
	public void timerInterrupt() {
		KThread.currentThread().yield();

		long nowTime = Machine.timer().getTime();

		while (true)
		{
			element nowElement = waitQueue.peek();
			if (nowElement == null) break;
			long nowT = nowElement.getWakeUptime();
			if (nowT <= nowTime)
			{
				waitQueue.poll();
				nowElement.getThread().ready();
			}
			else break;
		}
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks,
	 * waking it up in the timer interrupt handler. The thread must be
	 * woken up (placed in the scheduler ready set) during the first timer
	 * interrupt where
	 *
	 * <p><blockquote>
	 * (current time) >= (WaitUntil called time)+(x)
	 * </blockquote>
	 *
	 * @param	x	the minimum number of clock ticks to wait.
	 *
	 * @see	nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		//long wakeTime = Machine.timer().getTime() + x;
		//while (wakeTime > Machine.timer().getTime())
		//	KThread.yield();

		boolean status = Machine.interrupt().disable();

		KThread currentThread = KThread.currentThread();

		long wakeUpTime = Machine.timer().getTime()+x;
		waitQueue.add(new element(currentThread,wakeUpTime));

		currentThread.sleep();

		Machine.interrupt().restore(status);
	}

	private class element
	{
		private KThread thread;
		private long wakeUpTime;

		public element(KThread thread,long wakeUpTime)
		{
			this.thread = thread;
			this.wakeUpTime = wakeUpTime;
		}

		public long getWakeUptime()
		{
			return this.wakeUpTime;
		}

		public KThread getThread()
		{
			return this.thread;
		}
	}

	Comparator<element> orderer = new Comparator<element>() {
		public int compare(element a,element b)
		{
			long t1 = a.getWakeUptime();
			long t2 = a.getWakeUptime();
			if (t1==t2) return 0;
			if (t1<t2) return 1;
			else return -1;
		}
	};

	Queue<element> waitQueue =  new PriorityQueue<element>(100,orderer);  
}
