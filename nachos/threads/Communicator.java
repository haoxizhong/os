package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 *
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 *
	 * @param	word	the integer to transfer.
	 */
	public void speak(int word) {
		lock.acquire();

		numWaitingSpeaker ++;
		while (existActiveSpeaker || !existActiveListener)
			speakerCondition.sleep();
		numWaitingSpeaker --;
		existActiveSpeaker = true;

		this.word = word;

		talkCondition.wake();
		talkCondition.sleep();

		existActiveSpeaker = false;
		if (numWaitingSpeaker != 0) speakerCondition.wake();

		lock.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return
	 * the <i>word</i> that thread passed to <tt>speak()</tt>.
	 *
	 * @return	the integer transferred.
	 */    
	public int listen() {
		lock.acquire();

		numWaitingListener ++;
		while (existActiveListener)
			listenerCondition.sleep();
		numWaitingListener --;
		existActiveListener = true;

		speakerCondition.wake();
		talkCondition.sleep();

		int answer = word;

		talkCondition.wake();

		existActiveListener = false;

		if (numWaitingListener != 0) listenerCondition.wake();

		lock.release();

		return answer;
	}

	int word;

	boolean existActiveSpeaker = false;
	boolean existActiveListener = false;

	int numWaitingSpeaker = 0;
	int numWaitingListener = 0;

	private Lock lock = new Lock(); 
	private Condition2 speakerCondition = new Condition2(lock);
	private Condition2 listenerCondition = new Condition2(lock);
	private Condition2 talkCondition = new Condition2(lock);
}
