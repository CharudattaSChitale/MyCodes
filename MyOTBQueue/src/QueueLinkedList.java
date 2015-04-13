


public class QueueLinkedList {
	 public Node head, tail;
	    int size=0;
	    public QueueLinkedList(){
	    	head=new Node();
	    	tail=head;
	    	
	    }
	    

	    

	    public void enqueue(int ele)
	    {
	    	Node newNode=new Node(ele);
	    	tail.next=newNode;
	    	tail=newNode;
	    	size++;
	        
	    }
}
