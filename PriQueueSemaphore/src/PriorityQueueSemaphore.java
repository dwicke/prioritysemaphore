
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;



/**
 * 
 * @author Craig Knudsen <cknudsen@gmu.edu>, Drew Wicke
 * 
 */
public class PriorityQueueSemaphore {

	private binarySemaphore mySemaphore; // provides mut exclusion for the shared vars
	
	private PriorityQueue<Integer> queue;// shared var
	private TreeMap<Integer, QueueElement > priorityToID;// used to provide FCFS when priorities are equal
	private int numPermits; // shared var
	private long ids = 0; // shared var
	private Incrementer queMonitor; // to make sure bounded waiting is satisfied.
	
	// Constructor (default)
	public  PriorityQueueSemaphore ( int permits ) {
		mySemaphore = new binarySemaphore(1);
		numPermits = permits;
		queue = new PriorityQueue<Integer>();
		priorityToID = new TreeMap<Integer, QueueElement>();
		queMonitor = new Incrementer();
		queMonitor.start();
	}

	// Constructor where longer wait bumps up the pri queue up one level
	public PriorityQueueSemaphore ( int permits, int msecToElevatePriority ) {
		mySemaphore = new binarySemaphore(1);
		numPermits = permits;
		queue = new PriorityQueue<Integer>();
		queMonitor = new Incrementer(msecToElevatePriority);
		priorityToID = new TreeMap<Integer, QueueElement>();
		queMonitor.start();
	}

	/**
	 * uses the default delay
	 * @param priority
	 */
	public void P ( Integer priority ) {
		
		
		mySemaphore.P();
		
		if (numPermits > 0) {
			// give permit
			numPermits--;
			mySemaphore.V();
		}
		else {
			queue.add(priority);
			if (!priorityToID.containsKey(priority)) {
				
				QueueElement el = new QueueElement(priority);
				priorityToID.put(priority, el);
			}
			priorityToID.get(priority).block(mySemaphore);// wait in line
		}
		
		
	}
	
	
	/**
	 * release the permit and notify a thread in the queue
	 */
	public void V() {
		mySemaphore.P();
		
		
		
		
		// let the highest priority thread in
		if (!queue.isEmpty() && !priorityToID.isEmpty()) {
			int i = queue.remove();
			priorityToID.get(i).unBlock();
			if(!priorityToID.get(i).anyWaiting()) {
				priorityToID.remove(i);
			}
		} else {
			numPermits++;// no one is waiting so add my permit back.
		}
		
		
		
		mySemaphore.V();
	}
	
	/**
	 * Must call this method to shut down the semaphore...
	 */
	public void shutdown() {
		queMonitor.shouldContinue = false;
	}
	

	public final class Incrementer extends TDThread {
		private int msecToElevatePriority = 100; // shared var
		public boolean shouldContinue = true;
		public Incrementer() {
			
		}
		public Incrementer(int sec) {
			msecToElevatePriority = 100;
		}
		@Override
		public void run() {
			
			///when updating the priorities need to update the
			// priority queue
			
			while(shouldContinue) {
				try {
					Thread.sleep(msecToElevatePriority);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// update the priorities
				
				mySemaphore.P();// must change the queues
				// start at the high priority and merge it
				// with the next lower priority
				
				TreeMap<Integer, QueueElement> newPriMap = new TreeMap<Integer, QueueElement>();
				PriorityQueue<Integer> newqueue = new PriorityQueue<Integer>();
				for (Map.Entry<Integer, QueueElement> el : priorityToID.entrySet()) {
					
					int newKey = el.getKey() == 0 ? 0 : el.getKey() - 1;
					
					if (newPriMap.containsKey(newKey)) {
						// then merge
						newPriMap.get(newKey).merge(el.getValue());
						
					} else {
						newPriMap.put(newKey, el.getValue());
					}
					newqueue.add(newKey);
				}
				priorityToID.clear();
				queue.clear();
				priorityToID = newPriMap;
				queue = newqueue;
				mySemaphore.V();
			}
			
			
		}
	}
	
}
