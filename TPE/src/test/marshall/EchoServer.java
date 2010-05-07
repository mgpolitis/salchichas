package test.marshall;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import marshall.ServerReactor;
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
		m.setDest(m.origin);
		ret.add(m);
		return ret;
	}

	public static void main(String[] args) throws IOException {
		ServerReactor reactor = ServerReactor.getInstance();
		EchoServer s = new EchoServer();
		reactor.subscribeTCPServer(s, 8085);
		reactor.runServer();
	}

}