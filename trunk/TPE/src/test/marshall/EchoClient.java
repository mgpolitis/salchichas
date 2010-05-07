package test.marshall;

import java.util.List;

import marshall.interfaces.BaseClient;
import marshall.model.Message;

public class EchoClient implements BaseClient {

	@Override
	public Message createMessage(byte[] serialized) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message greet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Message> messageReceived(Message m) {
		// TODO Auto-generated method stub
		return null;
	}

}
