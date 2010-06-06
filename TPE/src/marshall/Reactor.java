package marshall;

import java.io.IOException;

import marshall.base.BaseClient;
import marshall.base.BaseServer;

public class Reactor implements ClientContainer, ServerContainer {

	private static final Reactor instance = new Reactor();

	private ClientReactor clientReactor = ClientReactor.getInstance();
	private TCPServerReactor serverReactor = TCPServerReactor.getInstance();

	private Reactor() {
	}

	public static Reactor getInstance() {
		return instance;
	}

	public void subscribeTCPClient(BaseClient client, String serverHost,
			int serverPort) throws IOException {
		this.clientReactor.subscribeTCPClient(client, serverHost, serverPort);
	}

	public void subscribeUDPClient(BaseClient client, String serverHost,
			int serverPort) {
		this.clientReactor.subscribeUDPClient(client, serverHost, serverPort);
	}

	public void subscribeTCPServer(BaseServer server, int listenPort)
			throws IOException {
		this.serverReactor.subscribeTCPServer(server, listenPort);
	}

	public void subscribeUDPServer(BaseServer server, int listenPort)
			throws IOException {
		this.serverReactor.subscribeUDPServer(server, listenPort);
	}

	public void run() throws IOException {
		this.serverReactor.runServer();
		this.clientReactor.runClient();
	}

}
