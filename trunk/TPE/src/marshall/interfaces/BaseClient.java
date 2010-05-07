package marshall.interfaces;

import java.util.List;

import marshall.model.Message;

public interface BaseClient {

	public List<Message> messageReceived(Message m);
	
	public Message greet();
	
	public Message createMessage(byte[] serialized);

}
