package marshall;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import marshall.interfaces.BaseClient;
import marshall.model.EndPoint;
import marshall.model.Message;

public class ClientReactor {

	private static final ClientReactor instance = new ClientReactor();
	// private static final int backlog = 50;
	// private Map<EndPoint,Socket> clients = new HashMap<EndPoint, Socket>();

	Socket clientSocket;

	BaseClient tcpSenderClient;
	EndPoint serverEndPoint;

	public static ClientReactor getInstance() {
		return instance;
	}

	private ClientReactor() {
	}

	public void subscribeTCPClient(BaseClient client, String serverHost,
			int serverPort) throws IOException {
		if (tcpSenderClient != null)
			throw new RuntimeException(
					"Can't subscribe more than one TCP client in reactor");

		this.serverEndPoint = new EndPoint(serverHost, serverPort);
		tcpSenderClient = client;
	}

	public void runClient() throws IOException {
		final ClientReactor thiz = this;

		System.out.println("Starting client "
				+ this.tcpSenderClient.getClass().getName()
				+ " connecting to port " + this.serverEndPoint.port + ".");

		clientSocket = new Socket(serverEndPoint.host, serverEndPoint.port);

		new Thread(new Runnable() {
			public void run() {
				try {
					String serverAddress = clientSocket
							.getRemoteSocketAddress().toString();
					System.out.printf("Connected to server %s\n",
							serverAddress);
					thiz.handle(clientSocket);
					if (!clientSocket.isClosed()) {
						clientSocket.close();
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}).start();

	}

	protected void handle(Socket socket) throws IOException {

		this.sendMessage(tcpSenderClient.greet());

		while (true) {
			Message incomingMessage = this.readMessage();
			List<Message> responses = tcpSenderClient
					.messageReceived(incomingMessage);
			if (responses == null) {
				break;
			}
			for (Message m : responses) {
				this.sendMessage(m);
			}
		}

		return;

	}

	private void sendMessage(Message m) throws IOException {
		final DataOutputStream w = new DataOutputStream(clientSocket
				.getOutputStream());

		byte[] serializedMessage = m.serialize();
		w.writeInt(serializedMessage.length);
		w.write(serializedMessage);
	}

	protected Message readMessage() throws IOException {
		final DataInputStream r = new DataInputStream(clientSocket
				.getInputStream());

		int length = r.readInt();
		byte[] serializedMessage = new byte[length];
		r.readFully(serializedMessage);
		Message m = tcpSenderClient.createMessage(serializedMessage);
		m.setOrigin(serverEndPoint);		
		return m;
	}

}
