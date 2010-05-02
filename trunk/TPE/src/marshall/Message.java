package marshall;


public abstract class Message {

	public String host = null;
	public Integer port = null;
	
	public abstract byte[] serialize();
	
	public Message(byte[] data) {
		this.loadData(data);
	}

	public abstract void loadData(byte[] data);
	
}
