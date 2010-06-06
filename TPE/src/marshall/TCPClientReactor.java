package marshall;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import marshall.base.BaseClient;
import marshall.interfaces.ClientContainer;
import marshall.model.EndPoint;
import marshall.model.Message;

public class TCPClientReactor implements ClientContainer {

	private static final TCPClientReactor instance = new TCPClientReactor();
	private static final int THREADS_IN_POOL = 10;

	Map<EndPoint, Socket> clientSockets = new HashMap<EndPoint, Socket>();
	Map<EndPoint, BaseClient> tcpSenderClients = new HashMap<EndPoint, BaseClient>();

	public static TCPClientReactor getInstance() {
		return instance;
	}

	private TCPClientReactor() {
	}

	public void subscribeTCPClient(BaseClient client, String serverHost,
			int serverPort) throws IOException {

		EndPoint serverEndPoint = new EndPoint(serverHost, serverPort);
		tcpSenderClients.put(serverEndPoint, client);
		client.setContainer(this);

	}

	public void subscribeUDPClient(BaseClient client, String serverHost,
			int serverPort) {
		throw new IllegalArgumentException("Protocol not yet supported");
	}

	public void runClient() {
		final TCPClientReactor thiz = this;
		final ExecutorService es = Executors
				.newFixedThreadPool(THREADS_IN_POOL);

		if (this.tcpSenderClients.size() == 0) {
			System.out.println("No TCP clients to start. (none subscribed)");
			return;
		}

		for (final EndPoint serverEndPoint : this.tcpSenderClients.keySet()) {
			final BaseClient tcpSenderClient = this.tcpSenderClients
					.get(serverEndPoint);
			System.out.println("Starting TCP client "
					+ tcpSenderClient.getClass().getName()
					+ " connecting to port " + serverEndPoint.port + ".");

			final Socket clientSocket;
			try {
				clientSocket = new Socket(serverEndPoint.host,
						serverEndPoint.port);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
				System.out.println("Could not resolve ip for address: "
						+ serverEndPoint);
				continue;
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("No server response for address: "
						+ serverEndPoint);
				continue;
			}
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
		if (greeting != null) {
			if (greeting.dest == null) {
				greeting.dest = serverEndPoint;
			}
			this.sendMessage(greeting);
		}

		boolean continueReading = true;
		while (continueReading) {
			Message incomingMessage = this.readMessage(serverEndPoint);
			List<Message> responses = actualClient
					.messageReceived(incomingMessage);
			if (responses == null) {
				continueReading = false;
			}
			for (Message m : responses) {
				if (m.dest == null) {
					m.dest = serverEndPoint;
				}
				try {
					this.sendMessage(m);
				} catch (SocketException e) {
					System.out.println("Server closed connection");
					continueReading = false;
				}
			}
		}

		return;

	}

	public void sendMessage(Message m) throws IOException {
		EndPoint dest = m.dest;
		Socket clientSocket = clientSockets.get(dest);
		final DataOutputStream w = new DataOutputStream(clientSocket
				.getOutputStream());

		final byte[] serializedMessage = m.serialize();
		synchronized (clientSocket) {
			w.writeInt(serializedMessage.length);
			w.write(serializedMessage);
		}
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
