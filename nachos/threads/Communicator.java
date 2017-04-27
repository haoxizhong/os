//yhdxt`oi`offt`of{inofinofmhphofx`ofxholhofuh`ov`ofphorih
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {
	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
		lock = new Lock();

		speakerCondition = new Condition2(lock);
		listenerCondition = new Condition2(lock);
		returnCondition = new Condition2(lock);

		AS = WS = AL = WL = 0;
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 * 
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 * 
	 * @param word
	 *            the integer to transfer.
	 */
	public void speak(int word) {
		lock.acquire();
		while (AS != 0) {
			WS++;
			speakerCondition.sleep();
			WS--;
		}

		AS++;

		this.word = word;

		if (AL != 0)
			returnCondition.wake();
		else {
			if (WL != 0)
				listenerCondition.wake();
			returnCondition.sleep();

			AS--;
			AL--;

			if (WS != 0)
				speakerCondition.wake();
		}

		lock.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		lock.acquire();

		while (AL != 0) {
			WL++;
			listenerCondition.sleep();
			WL--;
		}

		AL++;

		if (AS != 0)
			returnCondition.wake();
		else {
			if (WS != 0)
				speakerCondition.wake();
			returnCondition.sleep();

			AL--;
			AS--;

			if (WL != 0)
				listenerCondition.wake();
		}
		int word = this.word;

		lock.release();

		return word;
	}

	private static class Speaker implements Runnable {
		Communicator com;
		int index;
		int message;
		Speaker(int _index, Communicator _com, int _message) {
			index = _index;
			com = _com;
			message = _message;
		}
		public void run() {
			System.out.println("Speaker " + index + " starts speaking");
			com.speak(message);
			System.out.println("Speaker " + index + " speaks "+ message);
			System.out.println("Speaker " + index + " ends speaking");
		}
	}
	private static class Listener implements Runnable {
		int index;
		Communicator com;
		Listener(int _index, Communicator _com) {
			index = _index;
			com = _com;
		}
		public void run() {
			System.out.println("Listener " + index + " starts listening");
			int temp = com.listen();
			System.out.println("Listener " + index + " hears " + temp + " from speaker "+ temp);
			System.out.println("Listener " + index + " ends listening");
		}
	}
	/**
	 * Tools for test Communicator
	 */
	public static void selfTest() {
		Communicator com = new Communicator();
		/* case 1 
		(new KThread(new Listener(1, com))).fork();
		(new KThread(new Listener(2, com))).fork();
		(new KThread(new Speaker(1, com, 1))).fork();
		(new KThread(new Speaker(2, com, 2))).fork();
		(new KThread(new Speaker(3, com, 3))).fork();
		(new KThread(new Listener(3, com))).fork();
		*/
		///* case 2
		for(int i = 1; i < 6; i++){
			(new KThread(new Listener(i, com))).fork();
		}
		for(int i = 5; i > 0; i--){
			(new KThread(new Speaker(i, com, i))).fork();	
		}
		//*/
		ThreadedKernel.alarm.waitUntil(100000);
	}
	
	Lock lock;
	Condition2 speakerCondition, listenerCondition, returnCondition;
	int AS, WS, AL, WL;
	int word;
}
