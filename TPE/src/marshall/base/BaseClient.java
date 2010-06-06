package marshall.base;

import java.io.IOException;
import java.util.List;

import marshall.model.Message;

public abstract class BaseClient {

	
	protected void sendMessage(Message m) throws IOException {
		//TODO: do
	}
	
	public abstract List<Message> messageReceived(Message m);
	
	public abstract Message greet();
	
	public abstract Message createMessage(byte[] serialized);

}
