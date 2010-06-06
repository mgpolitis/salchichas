package marshall;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import marshall.base.BaseClient;
import marshall.interfaces.ClientContainer;
import marshall.model.EndPoint;
import marshall.model.Message;

public class UDPClientReactor implements ClientContainer {

	private static final UDPClientReactor instance = new UDPClientReactor();
	private static final int THREADS_IN_POOL = 10;
	private static final int DATAGRAM_BUF_SIZE = 1024;

	Map<EndPoint, DatagramSocket> clientSockets = new HashMap<EndPoint, DatagramSocket>();
	Map<EndPoint, BaseClient> udpSenderClients = new HashMap<EndPoint, BaseClient>();

	private byte[] buf = new byte[DATAGRAM_BUF_SIZE];

	public static UDPClientReactor getInstance() {
		return instance;
	}

	private UDPClientReactor() {
	}

	public void subscribeTCPClient(BaseClient client, String serverHost,
			int serverPort) throws IOException {
		throw new IllegalArgumentException("Protocol not yet supported");
	}

	public void subscribeUDPClient(BaseClient client, String serverHost,
			int serverPort) {
		EndPoint serverEndPoint = new EndPoint(serverHost, serverPort);
		udpSenderClients.put(serverEndPoint, client);
		client.setContainer(this);
	}

	public void runClient() {
		final UDPClientReactor thiz = this;
		final ExecutorService es = Executors
				.newFixedThreadPool(THREADS_IN_POOL);

		if (this.udpSenderClients.size() == 0) {
			System.out.println("No UDP clients to start. (none subscribed)");
			return;
		}

		for (final EndPoint serverEndPoint : this.udpSenderClients.keySet()) {
			final BaseClient tcpSenderClient = this.udpSenderClients
					.get(serverEndPoint);
			System.out.println("Starting UDP client "
					+ tcpSenderClient.getClass().getName() + ".");

			final DatagramSocket clientSocket;
			try {
				clientSocket = new DatagramSocket();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Couldn't open UDP socket for "
						+ serverEndPoint.host);
				continue;
			}
			clientSockets.put(serverEndPoint, clientSocket);

			Runnable r = new Runnable() {
				public void run() {
					try {
						String serverAddress = clientSocket
								.getRemoteSocketAddress().toString();
						System.out.printf(
								"Ready to communicate with server %s\n",
								serverAddress);
						thiz.handle(serverEndPoint);
						if (!clientSocket.isClosed()) {
							clientSocket.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						clientSockets.remove(serverEndPoint);
						udpSenderClients.remove(serverEndPoint);
					}
				}
			};
			es.execute(r);
		}

	}

	protected void handle(EndPoint serverEndPoint) throws IOException {
		BaseClient actualClient = udpSenderClients.get(serverEndPoint);

		Message greeting = actualClient.greet();
		if (greeting.dest == null) {
			greeting.dest = serverEndPoint;
		}
		this.sendMessage(greeting);

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

		final DatagramSocket socket = clientSockets.get(m.dest);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final DataOutputStream w = new DataOutputStream(baos);

		final InetAddress addr = InetAddress.getByName(m.dest.host);
		final int port = m.dest.port;

		final byte[] serializedMessage = m.serialize();
		w.writeInt(serializedMessage.length);
		w.write(serializedMessage);
		w.flush();
		final byte[] result = baos.toByteArray();
		final DatagramPacket datagram = new DatagramPacket(result,
				result.length, addr, port);
		synchronized (socket) {
			socket.setBroadcast(m.broadcastMe);
			socket.send(datagram);
		}

	}

	protected Message readMessage(EndPoint serverEndPoint) throws IOException {
		final DatagramPacket datagram = new DatagramPacket(buf, buf.length);
		final DatagramSocket udpSocket = this.clientSockets.get(serverEndPoint);
		try {
			udpSocket.receive(datagram);
		} catch (IOException e) {
			System.out.println("I/O error while reading UDP socket.");
			e.printStackTrace();
		}
		final DataInputStream r = new DataInputStream(new ByteArrayInputStream(
				datagram.getData()));
		final BaseClient udpSenderClient = udpSenderClients.get(serverEndPoint);
		final int length = r.readInt();
		byte[] serializedMessage = new byte[length];
		r.readFully(serializedMessage);
		Message m = udpSenderClient.createMessage(serializedMessage);
		m.origin = serverEndPoint;
		return m;
	}

	protected EndPoint getEndPointFromDatagram(DatagramPacket datagram) {
		String host = datagram.getAddress().getHostAddress();
		int port = datagram.getPort();
		return new EndPoint(host, port);
	}

}
