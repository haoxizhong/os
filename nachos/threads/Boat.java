//vtyt`oedofrihbit`omhqdhmtoyhofyhomh`ofkh`vdo
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    static boolean v1, v2, closed, apass;
    static int passenger, population, count;
    static Lock l1, l2, port, poplock;
    static Condition hey, start, wa1, wa2, bench, pass, rest, waiting;
    
    public static void selfTest()
    {
    	
	FakeGrader b = new Boat.FakeGrader();
	
	System.out.println("\n ***Testing Boats with several children and several adults***");
	begin(10, 10, b);
        
    b.checkValid();
//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
	
    }

	static class FakeGrader extends BoatGrader {
		public int a, b;
		public boolean where, valid, rider;
		
		public FakeGrader() {
			a = b = 0;
			where = true;
			valid = true;
			rider = false;
		}

		public void initializeChild(){
			System.out.println("A child has forked.");
			a++;
		}

		public void initializeAdult(){
			System.out.println("An adult as forked.");
			b++;
		}

	    public void ChildRowToMolokai() {
		System.out.println("**Child rowing to Molokai.");
		a--;
		if((a<0)||(b<0)) valid = false;
		rider = true;
		if(!where) {
			valid = false;
			System.out.println("1");
		}
		where = !where;
	    }

	    public void ChildRowToOahu() {
		System.out.println("**Child rowing to Oahu.");
		a++;
		if((a<0)||(b<0)) valid = false;
		rider = true;
		if(where) {
			valid = false;
			System.out.println("2");
		}
		where = !where;
	    }

	    public void ChildRideToMolokai() {
		System.out.println("**Child arrived on Molokai as a passenger.");
		a--;
		if((a<0)||(b<0)) valid = false;
		if(!rider) {
			valid = false;
			System.out.println("3");
		}
		rider = false;
	    }

	    public void ChildRideToOahu() {
		System.out.println("**Child arrived on Oahu as a passenger.");
		a++;
		if((a<0)||(b<0)) valid = false;
		if(!rider) {
			valid = false;
			System.out.println("4");
		}
		rider = false;
	    }

	    public void AdultRowToMolokai() {
		System.out.println("**Adult rowing to Molokai.");
		b--;
		if((a<0)||(b<0)) valid = false;
		if(!where) {
			valid = false;
			System.out.println("5");
		}
		rider = false;
		where = !where;
	    }

	    public void AdultRowToOahu() {
		System.out.println("**Adult rowing to Oahu.");
		b++;		
		if((a<0)||(b<0)) valid = false;
		if(where) {
			valid = false;
			System.out.println("6");
		}
		rider = false;
		where = !where;
	    }

	    public void AdultRideToMolokai() {
		System.out.println("**Adult arrived on Molokai as a passenger.");
		b--;
		// =\\\=
	    }

	    public void AdultRideToOahu() {
		System.out.println("**Adult arrived on Oahu as a passenger.");
		b++;
		// =///=
	    }
	    
	    public void checkValid() {
	    	if((a == 0)&&(b == 0)&&valid) System.out.println("Valid");
	    	else System.out.println("Invalid");
	    }

	}
    
    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;
	
	v1 = v2 = closed = apass = false;
	passenger = count  = 0;// Global variables used for the game
	population = 0;
	
	l1 = new Lock();
	l2 = new Lock();
	port = new Lock();
	poplock = new Lock();
	
	hey = new Condition(poplock);
	start = new Condition(poplock);
	wa1 = new Condition(l1);
	wa2 = new Condition(l2);
	bench = new Condition(port);
	pass = new Condition(port);
	rest = new Condition(port);
	waiting = new Condition(port);

	// Instantiate global variables here

	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.

	Runnable rc = new Runnable() {
	    public void run() {
                ChildItinerary();
            }
	    };
        
    Runnable ra = new Runnable() {
        	public void run() {
        		AdultItinerary();
        	}
        };
    
        KThread[] tc = new KThread[children];
        KThread[] ta = new KThread[adults];
        
        for(int i = 0; i < children; i++)
        	tc[i] = new KThread(rc);
        
        for(int i = 0; i < adults; i++)
        	ta[i] = new KThread(ra);
        
        for(int i = 0; i < children; i++)
        	tc[i].fork();
        
        for(int i = 0; i < adults; i++)
        	ta[i].fork();
        
        poplock.acquire();
        while(population != adults + children) {
        	hey.sleep();
        }
        start.wakeAll();
        poplock.release();
        
        for(int i = 0; i < children; i++)
        	tc[i].join();
        
        for(int i = 0; i < adults; i++)
        	ta[i].join();
        
        System.out.println("Current population: "+population);
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
	
	poplock.acquire();
	population++;
	hey.wake();
	start.sleep();
	poplock.release();
	
	boolean flag = false;
	while(true) {		
		l1.acquire();
		if(v1 == true) flag = true;
		else wa1.sleep();
		l1.release();
		
		if(flag) {
			l2.acquire();
			if(v2 == true) flag = true;
			else {
				flag = false;
				wa2.sleep();
			}
			l2.release();
			if(flag) break;
		}
	}//as known that the port is ready for use
	
	boolean passed = false;
	while(true) {
		port.acquire();
		if(closed == false) {
			passed = true;
			closed = true;
			passenger = 2;
			count++;
			rest.wake();
			pass.wake();
			bench.sleep();// the one with the ticket sleeps on the bench
			bg.AdultRowToMolokai();
			population--;
			pass.wake();
		}
		else waiting.sleep();// those with no tickets wait here ...
		port.release();
		if(passed) break;
	}
	
    }

    static void ChildItinerary()
    {
    	
	bg.initializeChild(); //Required for autograder interface. Must be the first thing called.
	//DO NOT PUT ANYTHING ABOVE THIS LINE.

	poplock.acquire();
	population++;
	hey.wake();
	start.sleep();
	poplock.release();
	
	int place = 0;
	
	l1.acquire();
	if(v1 == false) {
		v1 = true;
		place = 1;
		wa1.wakeAll();
	}
	l1.release();
	
	l2.acquire();
	if((place == 0)&&(v2 == false)) {
		v2 = true;
		place = 2;
		wa2.wakeAll();
	}
	l2.release(); //so much for competing for the leaders
	
	if(place == 0) {// the actions of other children
		boolean passed = false;
		while(true) {
			port.acquire();
			if(closed == false) {
				passed = true;
				closed = true;
				passenger = 1;
				count++;
				rest.wake();
				pass.wake();
				bench.sleep();// the one with the ticket sleeps on the bench
				bg.ChildRideToMolokai();
				population--;
				pass.wake();
			}
			else waiting.sleep();// those with no tickets wait here ...
			port.release();
			if(passed) break;
		}
	}
	
	else if(place == 1) {// the job of leader A
		int counting = 0;
		boolean ready = false;
		while(population > 2) {
			while(true) {
				port.acquire();
				if(count > counting) {
					counting++;
					if(passenger == 1) {
						if(population > 2) rest.sleep();
						else ready = true;
					}
					else if(passenger == 2) {
						bg.ChildRowToMolokai();
						population--;
						apass = true;
						pass.wake();
						rest.sleep();
						bg.ChildRowToOahu();
						population++;
						apass = false;
						bench.wake();
						rest.sleep();
					}
				}
				else {
					if(population > 2) rest.sleep();
					else ready = true;
				}
				port.release();
				if(ready) break;
			}// find the passenger
			if(ready) break;
		}// transport everyone else
		System.out.println("A quit");
		
		port.acquire();
		bg.ChildRowToMolokai();
		population--;
		apass = true;
		pass.wake();
		port.release();
	}
	
	else {// the job of leader B
		int counting = 0;
		boolean ready = false;
		while(population > 2) {
			while(true) {
				port.acquire();
				if(count > counting) {
					counting++;
					if(passenger == 2) {
						while(apass == false) {
							pass.sleep();// in this case, B takes a rest first
						}
						bg.ChildRideToMolokai();
						population--;
						rest.wake();
						pass.sleep();
						bg.ChildRowToOahu();
						population++;
						closed = false;
						waiting.wake();
						System.out.println("One cycle finished with population = "+population);
						if(population > 2) {
							pass.sleep();
						}
						else {
							ready = true;
							rest.wake();
						}
					}
					else if(passenger == 1) {
						bg.ChildRowToMolokai();
						population--;
						bench.wake();
						pass.sleep();
						bg.ChildRowToOahu();
						population++;
						closed = false;
						waiting.wake();
						System.out.println("One cycle finished with population = "+population);
						if(population > 2) pass.sleep();
						else {
							ready = true;
							rest.wake();
						}
					}
				}
				else {
					if(population > 2) pass.sleep();
					else {
						ready = true;
						rest.wake();
					}
				}
				port.release();
				if(ready) break;
			}//find the passenger
			if(ready) break;
		}// transport everyone else
		System.out.println("B quit");

		port.acquire();
		while(apass == false) {
			pass.sleep();
		}
		bg.ChildRideToMolokai();
		population--;
		port.release();
		}
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

}
