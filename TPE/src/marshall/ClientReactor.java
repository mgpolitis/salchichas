package marshall;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import marshall.interfaces.BaseClient;
import marshall.model.EndPoint;
import marshall.model.Message;

public class ClientReactor {

	private static final ClientReactor instance = new ClientReactor();
	private static final int THREADS_IN_POOL = 10;

	Map<EndPoint, Socket> clientSockets = new HashMap<EndPoint, Socket>();
	Map<EndPoint, BaseClient> tcpSenderClients = new HashMap<EndPoint, BaseClient>();

	public static ClientReactor getInstance() {
		return instance;
	}

	private ClientReactor() {
	}

	public void subscribeTCPClient(BaseClient client, String serverHost,
			int serverPort) throws IOException {

		EndPoint serverEndPoint = new EndPoint(serverHost, serverPort);
		tcpSenderClients.put(serverEndPoint, client);

	}

	public void runClient() throws IOException {
		final ClientReactor thiz = this;
		final ExecutorService es = Executors
				.newFixedThreadPool(THREADS_IN_POOL);
		for (final EndPoint serverEndPoint : this.tcpSenderClients.keySet()) {
			final BaseClient tcpSenderClient = this.tcpSenderClients
					.get(serverEndPoint);
			System.out.println("Starting client "
					+ tcpSenderClient.getClass().getName()
					+ " connecting to port " + serverEndPoint.port + ".");

			final Socket clientSocket = new Socket(serverEndPoint.host,
					serverEndPoint.port);
			clientSockets.put(serverEndPoint, clientSocket);

			Runnable r = new Runnable() {
				public void run() {
					try {
						String serverAddress = clientSocket
								.getRemoteSocketAddress().toString();
						System.out.printf("Connected to server %s\n",
								serverAddress);
						thiz.handle(serverEndPoint);
						if (!clientSocket.isClosed()) {
							clientSocket.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						clientSockets.remove(serverEndPoint);
						tcpSenderClients.remove(serverEndPoint);
					}
				}
			};
			es.execute(r);
		}

	}

	protected void handle(EndPoint serverEndPoint) throws IOException {
		BaseClient actualClient = tcpSenderClients.get(serverEndPoint);

		Message greeting = actualClient.greet();
		if (greeting.dest == null) {
			greeting.dest = serverEndPoint;
		}
		this.sendMessage(greeting);

		while (true) {
			Message incomingMessage = this.readMessage(serverEndPoint);
			List<Message> responses = actualClient
					.messageReceived(incomingMessage);
			if (responses == null) {
				break;
			}
			for (Message m : responses) {
				if (m.dest == null) {
					m.dest = serverEndPoint;
				}
				this.sendMessage(m);
			}
		}

		return;

	}

	private void sendMessage(Message m) throws IOException {
		EndPoint dest = m.dest;
		Socket clientSocket = clientSockets.get(dest);
		final DataOutputStream w = new DataOutputStream(clientSocket
				.getOutputStream());

		final byte[] serializedMessage = m.serialize();
		w.writeInt(serializedMessage.length);
		w.write(serializedMessage);
	}

	protected Message readMessage(EndPoint serverEndPoint) throws IOException {
		Socket clientSocket = clientSockets.get(serverEndPoint);
		BaseClient tcpSenderClient = tcpSenderClients.get(serverEndPoint);
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
