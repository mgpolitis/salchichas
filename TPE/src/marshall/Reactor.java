package marshall;

import java.io.IOException;

import marshall.base.BaseClient;
import marshall.base.BaseServer;
import marshall.interfaces.ClientContainer;
import marshall.interfaces.ServerContainer;
import marshall.model.Message;

public class Reactor implements ClientContainer, ServerContainer {

	private static final Reactor instance = new Reactor();

	private TCPClientReactor tcpClientReactor = TCPClientReactor.getInstance();
	private TCPServerReactor tcpServerReactor = TCPServerReactor.getInstance();
	private UDPClientReactor udpClientReactor = UDPClientReactor.getInstance();
	private UDPServerReactor udpServerReactor = UDPServerReactor.getInstance();

	private Reactor() {
	}

	public static Reactor getInstance() {
		return instance;
	}

	public void subscribeTCPClient(BaseClient client, String serverHost,
			int serverPort) throws IOException {
		this.tcpClientReactor
				.subscribeTCPClient(client, serverHost, serverPort);
	}

	public void subscribeUDPClient(BaseClient client, String serverHost,
			int serverPort) {
		this.udpClientReactor
				.subscribeUDPClient(client, serverHost, serverPort);
	}

	public void subscribeTCPServer(BaseServer server, int listenPort)
			throws IOException {
		this.tcpServerReactor.subscribeTCPServer(server, listenPort);
	}

	public void subscribeUDPServer(BaseServer server, int listenPort)
			throws IOException {
		this.udpServerReactor.subscribeUDPServer(server, listenPort);
	}

	public void run() throws IOException {
		this.tcpClientReactor.runClient();
		this.tcpServerReactor.runServer();
		this.udpClientReactor.runClient();
		this.udpServerReactor.runServer();
	}

	@Override
	public void sendMessage(Message m) throws IOException {
		throw new IllegalStateException(
				"Can't invoke method on general reactor");
	}

}
