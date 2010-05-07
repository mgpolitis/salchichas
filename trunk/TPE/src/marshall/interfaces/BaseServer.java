package marshall.interfaces;

import java.util.List;

import marshall.model.Message;

public interface BaseServer {

	public List<Message> messageReceived(Message m);
	
	public Message createMessage(byte[] serialized);
	
}
