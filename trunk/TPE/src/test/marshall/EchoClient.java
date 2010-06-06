package test.marshall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import marshall.Reactor;
import marshall.base.BaseClient;
import marshall.model.Message;

public class EchoClient extends BaseClient {

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
		Reactor r = Reactor.getInstance();
		EchoClient c = new EchoClient();

		r.subscribeTCPClient(c, "localhost", 8085);
		r.run();
	}

}
