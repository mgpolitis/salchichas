package marshall;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import marshall.base.BaseServer;
import marshall.interfaces.ServerContainer;
import marshall.model.EndPoint;
import marshall.model.Message;

public class UDPServerReactor implements ServerContainer {

	private static final UDPServerReactor instance = new UDPServerReactor();
	private static final int THREADS_IN_POOL = 10;
	private static final int DATAGRAM_BUF_SIZE = 1024;

	private byte[] buf = new byte[DATAGRAM_BUF_SIZE];
	private BaseServer udpObserverServer;
	private DatagramSocket udpSocket;

	public static UDPServerReactor getInstance() {
		return instance;
	}

	private UDPServerReactor() {
	}

	private boolean udpInitialized() {
		return (udpSocket != null || udpObserverServer != null);
	}

	public void subscribeUDPServer(BaseServer server, int listenPort)
			throws IOException {
		if (this.udpInitialized())
			throw new RuntimeException(
					"Can't subscribe more than one UDP server in reactor");

		udpSocket = new DatagramSocket(listenPort);
		udpObserverServer = server;
		server.setContainer(this);
	}

	public void runServer() {
		final UDPServerReactor thiz = this;

		if (!this.udpInitialized()) {
			System.out.println("Starting no UDP servers. (none subscribed)");
			return;
		}

		System.out.println("Starting UDP server "
				+ this.udpObserverServer.getClass().getName() + " on "
				+ this.udpSocket.getLocalSocketAddress().toString() + ".");
		System.out.println("Now accepting clients...");
		final ExecutorService es = Executors
				.newFixedThreadPool(THREADS_IN_POOL);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {

					final DatagramPacket datagram = new DatagramPacket(buf,
							buf.length);
					try {
						udpSocket.receive(datagram);
					} catch (IOException e) {
						System.out
								.println("I/O error while reading UDP socket.");
						e.printStackTrace();
					}
					final EndPoint newClientEndPoint = thiz
							.getEndPointFromDatagram(datagram);

					Runnable runner = new Runnable() {
						public void run() {
							System.out.printf(
									"received message from client: %s\n",
									newClientEndPoint.host.toString());
							try {
								thiz.handle(datagram);
							} catch (IOException e) {
								e.printStackTrace();
								System.out
										.println("Could not read datagram from "
												+ newClientEndPoint);
							}

						}
					};
					es.execute(runner);

				}
			}
		}).start();

	}

	protected void handle(DatagramPacket datagram) throws IOException {
		Message incomingMessage = this.readMessage(datagram);
		List<Message> responses = udpObserverServer
				.messageReceived(incomingMessage);
		if (responses == null) {
			return;
		}
		for (Message m : responses) {
			if (m.dest == null) {
				throw new IllegalStateException(
						"Message must have a destination");
			}
			this.sendMessage(m);
		}
	}

	public void sendMessage(Message m) throws IOException {

		final DatagramSocket socket = this.udpSocket;
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

	protected Message readMessage(DatagramPacket datagram) throws IOException {

		final DataInputStream r = new DataInputStream(new ByteArrayInputStream(
				datagram.getData()));
		int length = r.readInt();
		byte[] serializedMessage = new byte[length];
		r.readFully(serializedMessage);
		Message m = udpObserverServer.createMessage(serializedMessage);
		m.origin = this.getEndPointFromDatagram(datagram);
		return m;
	}

	protected EndPoint getEndPointFromDatagram(DatagramPacket datagram) {
		String host = datagram.getAddress().getHostAddress();
		int port = datagram.getPort();
		return new EndPoint(host, port);
	}

	@Override
	public void subscribeTCPServer(BaseServer server, int listenPort)
			throws IOException {
		throw new IllegalArgumentException("Method not yet implemented");
	}

}
