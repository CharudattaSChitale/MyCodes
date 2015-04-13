import java.util.ArrayList;




public interface myQueueSet {
public boolean enq(int item) throws AbortedException;
	
	public boolean deq() throws AbortedException;
	
	public void commit() throws AbortedException;
	
	public boolean nontransactionalEnq(int item);
	
	public void abort();
	
	public Node getHead();
	
	public Node getTail();
}
