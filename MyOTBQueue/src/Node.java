import java.util.concurrent.atomic.AtomicInteger;




public class Node {
	public int value;
	public Node next;
	volatile AtomicInteger lock = new AtomicInteger(0);
	volatile long lockHolder = -1;
	public Node(int x) {
	value = x;
	next = null;
	}
	
	public Node(){
	}
}
