
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;


public class QueueElement implements Comparable<QueueElement>{

	private int priority;
	private LinkedList<binarySemaphore> queues;
	private LinkedList<AtomicInteger> numWaiting;
	
	public QueueElement(Integer priority) {
		this.priority = priority;
		queues = new LinkedList<binarySemaphore>();
		queues.add(new binarySemaphore(0));
		
		numWaiting = new LinkedList<AtomicInteger>();
		numWaiting.add(new AtomicInteger(0));
	}
	
	
	public void block() {
		numWaiting.peekLast().incrementAndGet();
		queues.peekLast().P();// must get in line at the end...
	}
	/**
	 * releases the passed semaphore and then blocks on a different semaphore.
	 * @param unblock
	 */
	public void block(binarySemaphore unblock) {
		numWaiting.peekLast().incrementAndGet();
		unblock.VP(queues.peekLast());// must get in line at the end...
	}
	
	public void unBlock() {
		
		numWaiting.peekFirst().decrementAndGet();
		//queue.V();
		// wake the guy up at the front
		queues.peekFirst().V();
		// remove old queues
		if(numWaiting.peekFirst().get() == 0 && !numWaiting.isEmpty()) {
			numWaiting.removeFirst();
			queues.removeFirst();
		}
		
	}
	
	public void merge(QueueElement element) {
		queues.addAll(element.getQueues());
		numWaiting.addAll(element.getSizes());
	}
	
	public boolean anyWaiting() {
		while(numWaiting.peekFirst().get() == 0 && !numWaiting.isEmpty()) {
			numWaiting.removeFirst();
			queues.removeFirst();
		}
		return !numWaiting.isEmpty() && numWaiting.peekFirst().get() > 0;
	}

	public Integer getPriority() {
		return priority;
	}
	
	public LinkedList<binarySemaphore> getQueues() {
		return queues;
	}
	public LinkedList<AtomicInteger> getSizes() {
		return numWaiting;
	}
	
	@Override
	public int compareTo(QueueElement o) {
		// lower # have higher priorities
		return getPriority().compareTo(o.getPriority()) * -1;
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj instanceof QueueElement) {
			QueueElement el = (QueueElement)obj;
			return ((QueueElement) obj).getPriority().equals(el.getPriority());
		}
		return false;
	}
	
}
