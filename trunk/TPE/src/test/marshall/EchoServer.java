package test.marshall;

import java.util.LinkedList;
import java.util.List;

import marshall.interfaces.BaseServer;
import marshall.model.Message;

public class EchoServer implements BaseServer {

	
	
	@Override
	public Message createMessage(byte[] serialized) {
		return new EchoMessage(null, null, serialized);
	}

	@Override
	public List<Message> messageReceived(Message m) {
		List<Message> ret = new LinkedList<Message>();
		ret.add(m);
		return ret;
	}

}
