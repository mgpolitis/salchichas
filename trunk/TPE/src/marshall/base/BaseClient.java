package marshall.base;

import java.util.List;

import marshall.model.Message;

public abstract class BaseClient {

	public abstract List<Message> messageReceived(Message m);
	
	public abstract Message greet();
	
	public abstract Message createMessage(byte[] serialized);

}
