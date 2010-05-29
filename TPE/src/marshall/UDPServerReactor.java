package marshall;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import marshall.interfaces.BaseServer;
import marshall.model.EndPoint;
import marshall.model.Message;

public class UDPServerReactor implements ServerContainer {

	private static final UDPServerReactor instance = new UDPServerReactor();
	private static final int BACKLOG = 50;
	private static final int THREADS_IN_POOL = 10;
	private static final int DATAGRAM_BUF_SIZE = 1000;

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
	}

	public void runServer() {
		final UDPServerReactor thiz = this;

		if (!this.udpInitialized()) {
			System.out.println("Starting no servers. (none subscribed)");
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

	private void sendMessage(Message m) throws IOException {
		
		//TODO: HACER BIEN ESTA MAL!!!!!!
		//TODO: HACER BIEN ESTA MAL!!!!!!
		//TODO: HACER BIEN ESTA MAL!!!!!!
		
		final DatagramSocket socket = this.udpSocket;

		final DataOutputStream w = new DataOutputStream(
				new ByteArrayOutputStream() {

				});

		byte[] serializedMessage = m.serialize();
		DatagramPacket datagram = new DatagramPacket(serializedMessage,
				serializedMessage.length);
		w.writeInt(serializedMessage.length);
		w.write(serializedMessage);
		//TODO: HACER BIEN ESTA MAL!!!!!!
		//TODO: HACER BIEN ESTA MAL!!!!!!
		//TODO: HACER BIEN ESTA MAL!!!!!!
		
	
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
