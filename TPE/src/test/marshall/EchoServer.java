package test.marshall;

import java.util.List;

import marshall.interfaces.BaseServer;
import marshall.model.Message;

public class EchoServer implements BaseServer {

	
	
	@Override
	public Message createMessage(byte[] serialized) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Message> messageReceived(Message m) {
		// TODO Auto-generated method stub
		return null;
	}

}
