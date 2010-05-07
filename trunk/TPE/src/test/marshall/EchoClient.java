package test.marshall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import marshall.ClientReactor;
import marshall.interfaces.BaseClient;
import marshall.model.Message;

public class EchoClient implements BaseClient {

	@Override
	public Message createMessage(byte[] serialized) {
		return new EchoMessage(null, null, serialized);
	}

	@Override
	public Message greet() {
		return this.getLine();
	}

	private Message getLine() {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(
				System.in));
		
		String line = "";
		try {
			line = stdin.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new EchoMessage(line);
	}

	@Override
	public List<Message> messageReceived(Message m) {
		System.out.println(m);
		List<Message> ret = new LinkedList<Message>();
		ret.add(this.getLine());
		return ret;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		ClientReactor reactor = ClientReactor.getInstance();
		EchoClient c = new EchoClient();
		
		reactor.subscribeTCPClient(c, "localhost", 8085);
		reactor.runClient();
	}

}
