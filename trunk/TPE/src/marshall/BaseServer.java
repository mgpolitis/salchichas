package marshall;

import java.util.List;

public interface BaseServer {

	public List<Message> messageReceived(Message m);
	
	public Message createMessage(byte[] serialized);
	
}
