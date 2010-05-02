package marshall;

import java.util.List;

public interface BaseClient {

	public List<Message> messageReceived(Message m);
	
	public Message greet();

}
