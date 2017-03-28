package nachos.threads;
import nachos.ag.BoatGrader;

import java.util.LinkedList;

public class Boat
{
	static BoatGrader bg;

	public static void selfTest()
	{
		BoatGrader b = new BoatGrader();

		System.out.println("\n ***Testing Boats with only 2 children***");
		begin(0, 2, b);

		//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		//  	begin(1, 2, b);

		//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		//  	begin(3, 3, b);
	}

	public static void begin( int adults, int children, BoatGrader b )
	{
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;

		// Instantiate global variables here

		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.

		/*Runnable r = new Runnable() {
			public void run() {
				SampleItinerary();
			}
		};
		KThread t = new KThread(r);
		t.setName("Sample Boat Thread");
		t.fork();*/

		adultsInOahu = adults;
		adultsInMolokai = 0;
		childrenInOahu = children;
		childrenInMolokai = 0;

		boatPosition = OAHU;

		LinkedList<KThread> adultsThread = new LinkedList<KThread>();
		LinkedList<KThread> childrenThread = new LinkedList<KThread>();

		for (int a = 1;a <= adults;a++)
			adultsThread.add(new KThread(new Runnable() {
				@Override
				public void run()
				{
					AdultItinerary();
				}
			}));
		for (int a = 1;a <= children;a++)
			childrenThread.add(new KThread(new Runnable() {
				@Override
				public void run()
				{
					ChildItinerary();
				}
			}));

		for (int a=0;a<adults;a++)
			adultsThread.get(a).setName("Adult:"+a).fork();

		for (int a=0;a<children;a++)
			childrenThread.get(a).setName("Child:"+a).fork();

//		while (!ended)
//		{
//			int word = communicator.listen();
//			if (word == adults + children) ended=true;
//		}

		for (int a=0;a<adults;a++)
			adultsThread.get(a).join();

		for (int a=0;a<children;a++)
			childrenThread.get(a).join();
	}

	static void AdultItinerary()
	{
		bg.initializeAdult(); //Required for autograder interface. Must be the first thing called.
		//DO NOT PUT ANYTHING ABOVE THIS LINE. 

		/* This is where you should put your solutions. Make calls
		   to the BoatGrader to show that it is synchronized. For
example:
bg.AdultRowToMolokai();
indicates that an adult has rowed the boat across to Molokai
		 */

		int nowPosition = OAHU;

		lock.acquire();

		while (nowPosition != MOLOKAI)
		{
			//System.out.println("adults here");
			if (childrenInOahu < 2 && nowPosition == boatPosition && !childOnBoat)
			{
				int rememberPeople = adultsInOahu + childrenInOahu;
				adultsInOahu --;
				adultsInMolokai ++;
				nowPosition = MOLOKAI;
				boatPosition = MOLOKAI;

				bg.AdultRideToMolokai();

				rememberPeople --;
				if (rememberPeople == 0) ended = true;

				molokai.wake();
			}
			else
			{
				oahu.wake();
				oahu.sleep();
			}
		}

		//communicator.speak(adultsInMolokai + childrenInMolokai);

		lock.release();
	}

	static void ChildItinerary()
	{
		bg.initializeChild(); //Required for autograder interface. Must be the first thing called.
		//DO NOT PUT ANYTHING ABOVE THIS LINE. 

		int nowPosition = OAHU;

		lock.acquire();

		while (!ended)
		{
			//if (nowPosition == MOLOKAI)
			//{
			//	System.out.println("child here");
			//	System.out.println(nowPosition+" "+boatPosition);
			//	System.out.println(childrenInOahu);
			//}
			if (nowPosition == boatPosition)
			{
				if (nowPosition == OAHU)
				{
					//System.out.println(childOnBoat);
					if (childrenInOahu >= 2)
					{
						if (!childOnBoat)
						{
							childOnBoat = true;
							nowPosition = MOLOKAI;

							bg.ChildRowToMolokai();
			
							oahu.wake();
							molokai.sleep();
						}
						else
						{
							int rememberPeople = adultsInOahu + childrenInOahu;
							
							childOnBoat = false;
							nowPosition = MOLOKAI;
							boatPosition = MOLOKAI;
							childrenInOahu -= 2;
							childrenInMolokai += 2;

							bg.ChildRideToMolokai();

							//communicator.speak(adultsInMolokai + childrenInMolokai);

							rememberPeople -= 2;
							if (rememberPeople == 0) ended = true;

							molokai.wake();
							if (!ended) molokai.sleep();
						}
					}
					else
					{
						oahu.wake();
						oahu.sleep();
					}
				}
				else
				{
					nowPosition = OAHU;
					boatPosition = OAHU;
					childrenInOahu ++;
					childrenInMolokai --;

					bg.ChildRideToOahu();

					oahu.wake();
					oahu.sleep();
				}
			}
			else
			{
				if (nowPosition == OAHU)
				{
					oahu.wake();
					oahu.sleep();
				}
				else
				{
					molokai.wake();
					molokai.sleep();
				}
			}
		}

		molokai.wakeAll();

		lock.release();
	}

	static void SampleItinerary()
	{
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}

	static final int OAHU = 1;
	static final int MOLOKAI = 2;

	static int boatPosition;

	static int adultsInOahu;
	static int adultsInMolokai;
	static int childrenInOahu;
	static int childrenInMolokai;

	static Lock lock = new Lock();
	//static Lock travelingLock = new Lock();

	static Condition oahu = new Condition(lock);
	static Condition molokai = new Condition(lock);
	//static Condition traveling = new Condition(travelingLock);

	static Communicator communicator = new Communicator();

	static boolean ended = false;
	static boolean childOnBoat = false;
}
