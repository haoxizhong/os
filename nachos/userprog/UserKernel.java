package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.LinkedList;
import java.util.HashMap;

/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {

	class zhxFile {
		String filename;
		int threadOpenIt = 0;
		boolean delete = false;
		public zhxFile(String name) {
			filename = name;
			threadOpenIt = 1;
		}
	}

	class FileManager {
		HashMap<String,zhxFile> map = new HashMap<String,zhxFile>();

		public boolean openFile(String name) {
			if (map.containsKey(name)) {
				zhxFile file = map.get(name);
				if (file.delete) return false;
				file.threadOpenIt ++;
			}
			else {
				map.put(name,new zhxFile(name));
			}
			return true;
		}

		public void closeFile(String name) {
			zhxFile file = map.get(name);
			file.threadOpenIt --;
			if (file.threadOpenIt == 0) {
				map.remove(file);
				if (file.delete == true) {
					UserKernel.fileSystem.remove(name);
				}
			}
		}

		public boolean removeFile(String name) {
			if (!map.containsKey(name)) {
				return true;
			}
			zhxFile file = map.get(name);
			if (file.threadOpenIt != 0) {
				file.delete = true;
			}
			else {
				UserKernel.fileSystem.remove(name);
				//System.out.println("I think it's impossible");
				//System.out.println("I don't know why mv can reach heer");
			}

			return true;
		}
	}

	public static FileManager fileManager = null;
	/**
	 * Allocate a new user kernel.
	 */
	public UserKernel() {
		super();
	}

	static LinkedList<Integer> pageList;

	/**
	 * Initialize this kernel. Creates a synchronized console and sets the
	 * processor's exception handler.
	 */
	public void initialize(String[] args) {
		super.initialize(args);

		console = new SynchConsole(Machine.console());

		Machine.processor().setExceptionHandler(new Runnable() {
				public void run() { exceptionHandler(); }
				});

		int numPhysical = Machine.processor().getNumPhysPages();

		pageList = new LinkedList<>();

		for (int a=0;a<numPhysical;a++)
			pageList.add(a);

		lock = new Lock();
		fileManager = new FileManager();
	}

	private static Lock lock;

	public static int getFreePage()
	{
		lock.acquire();
		int ppd = pageList.poll();
		lock.release();
		return ppd;
	}

	public static void addFreePage(int ppd)
	{
		lock.acquire();
		pageList.add(ppd);
		lock.release();
	}

	/**
	 * Test the console device.
	 */	
	public void selfTest() {
		super.selfTest();

		/*System.out.println("Testing the console device. Typed characters");
		System.out.println("will be echoed until q is typed.");

		char c;

		do {
			c = (char) console.readByte(true);
			console.writeByte(c);
		}
		while (c != 'q');

		System.out.println("");*/
	}

	/**
	 * Returns the current process.
	 *
	 * @return	the current process, or <tt>null</tt> if no process is current.
	 */
	public static UserProcess currentProcess() {
		if (!(KThread.currentThread() instanceof UThread))
			return null;

		return ((UThread) KThread.currentThread()).process;
	}

	/**
	 * The exception handler. This handler is called by the processor whenever
	 * a user instruction causes a processor exception.
	 *
	 * <p>
	 * When the exception handler is invoked, interrupts are enabled, and the
	 * processor's cause register contains an integer identifying the cause of
	 * the exception (see the <tt>exceptionZZZ</tt> constants in the
	 * <tt>Processor</tt> class). If the exception involves a bad virtual
	 * address (e.g. page fault, TLB miss, read-only, bus error, or address
	 * error), the processor's BadVAddr register identifies the virtual address
	 * that caused the exception.
	 */
	public void exceptionHandler() {
		Lib.assertTrue(KThread.currentThread() instanceof UThread);

		UserProcess process = ((UThread) KThread.currentThread()).process;
		int cause = Machine.processor().readRegister(Processor.regCause);
		//System.out.println(cause);
		process.handleException(cause);
	}

	/**
	 * Start running user programs, by creating a process and running a shell
	 * program in it. The name of the shell program it must run is returned by
	 * <tt>Machine.getShellProgramName()</tt>.
	 *
	 * @see	nachos.machine.Machine#getShellProgramName
	 */
	public void run() {
		super.run();

		UserProcess process = UserProcess.newUserProcess();

		String shellProgram = Machine.getShellProgramName();	
		/*String[] args = new String[2];
		args[0]="cat";
		args[1]="a.txt";*/
		String[] args = new String[0];
		Lib.assertTrue(process.execute(shellProgram, args));

		KThread.currentThread().finish();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
		super.terminate();
	}

	/** Globally accessible reference to the synchronized console. */
	public static SynchConsole console;

	// dummy variables to make javac smarter
	private static Coff dummy1 = null;
}
