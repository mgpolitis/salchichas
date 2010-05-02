package marshall;


public abstract class Message {

	public EndPoint origin;
	public EndPoint dest;
	
	public abstract byte[] serialize();
	
	public Message(EndPoint origin, EndPoint dest){
		this.origin = origin;
		this.dest = dest;
	}
	
	public Message(byte[] data) {
		this.loadData(data);
	}

	public abstract void loadData(byte[] data);
	
}
