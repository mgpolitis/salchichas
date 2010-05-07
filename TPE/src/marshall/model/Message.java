package marshall.model;

public abstract class Message {

	public EndPoint origin;
	public EndPoint dest;

	public abstract byte[] serialize();

	public Message() {
	}

	public void setOrigin(EndPoint origin) {
		this.origin = origin;
	}

	public void setDest(EndPoint dest) {
		this.dest = dest;
	}

	public Message(EndPoint origin, EndPoint dest, byte[] data) {
		this.origin = origin;
		this.dest = dest;
		this.loadData(data);
	}

	public abstract void loadData(byte[] data);

}
