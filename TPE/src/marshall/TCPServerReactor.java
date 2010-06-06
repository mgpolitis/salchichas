package marshall;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import marshall.base.BaseServer;
import marshall.interfaces.ServerContainer;
import marshall.model.EndPoint;
import marshall.model.Message;

public class TCPServerReactor implements ServerContainer {

	private static final TCPServerReactor instance = new TCPServerReactor();
	private static final int BACKLOG = 50;
	private static final int THREADS_IN_POOL = 10;

	private final ExecutorService excecutorService = Executors
			.newFixedThreadPool(THREADS_IN_POOL);
	private final Map<EndPoint, Socket> tcpClients = new HashMap<EndPoint, Socket>();
	private final Map<Integer, BaseServer> tcpObserverServers = new HashMap<Integer, BaseServer>();
	private final Map<Integer, ServerSocket> tcpServerSockets = new HashMap<Integer, ServerSocket>();

	public static TCPServerReactor getInstance() {
		return instance;
	}

	private TCPServerReactor() {
	}

	public void subscribeTCPServer(BaseServer server, int listenPort)
			throws IOException {

		tcpServerSockets.put(listenPort, new ServerSocket(listenPort,
				TCPServerReactor.BACKLOG, InetAddress.getByName("localhost")));
		tcpObserverServers.put(listenPort, server);
		server.setContainer(this);
	}

	public void subscribeUDPServer(BaseServer server, int listenPort)
			throws IOException {
		throw new IllegalArgumentException("Method not yet implemented");
	}

	private boolean tcpInitialized() {
		return !tcpServerSockets.isEmpty();
	}

	public void runServer() {

		if (!this.tcpInitialized()) {
			System.out.println("Starting no TCP servers. (none subscribed)");
			return;
		}

		for (int serverPort : tcpObserverServers.keySet()) {
			this.injectServer(serverPort);
		}

	}

	void injectServer(int serverPort) {
		final TCPServerReactor thiz = this;
		final BaseServer tcpObserverServer = tcpObserverServers.get(serverPort);
		final ServerSocket tcpServerSocket = tcpServerSockets.get(serverPort);

		System.out.println("Starting TCP server "
				+ tcpObserverServer.getClass().getName() + " on "
				+ tcpServerSocket.getLocalSocketAddress().toString() + ".");
		System.out.println("Now accepting clients...");
		Runnable listenerRunner = new Runnable() {
			@Override
			public void run() {
				while (true) {
					final Socket socket;
					try {
						socket = tcpServerSocket.accept();
					} catch (IOException e1) {
						e1.printStackTrace();
						return;
					}
					final EndPoint newClientEndPoint = thiz
							.getEndPointFromSocket(socket);
					tcpClients.put(newClientEndPoint, socket);

					Runnable handleRunner = new Runnable() {
						public void run() {
							try {
								System.out.printf("Client has connected: %s\n",
										socket.getRemoteSocketAddress()
												.toString());
								try {
									thiz.handle(tcpObserverServer,
											newClientEndPoint);
								} catch (EOFException e) {
									System.out
											.println("Client closed connection: "
													+ newClientEndPoint);
								}

								if (!socket.isClosed()) {
									socket.close();
								}
							} catch (IOException e) {
								e.printStackTrace();
							} finally {
								tcpClients.remove(newClientEndPoint);
							}

						}
					};
					excecutorService.execute(handleRunner);
				}
			}
		};
		excecutorService.execute(listenerRunner);
	}

	protected void handle(BaseServer tcpObserverServer, EndPoint clientEndPoint)
			throws IOException {
		final Socket socket = tcpClients.get(clientEndPoint);
		while (true) {
			Message incomingMessage = this.readMessage(tcpObserverServer,
					socket);
			List<Message> responses = tcpObserverServer
					.messageReceived(incomingMessage);
			if (responses == null) {
				break;
			}
			for (Message m : responses) {
				if (m.dest == null) {
					throw new IllegalStateException(
							"Message must have a destination");
				}
				this.sendMessage(m);
			}
		}
	}

	public void sendMessage(final Message m) throws IOException {
		Socket socket = tcpClients.get(new EndPoint(m.dest.host, m.dest.port));
		if (socket == null) {
			throw new IOException(
					"ERROR sending message: Endpoint not connected");
		}
		final DataOutputStream w = new DataOutputStream(socket
				.getOutputStream());

		byte[] serializedMessage = m.serialize();
		synchronized (socket) {
			w.writeInt(serializedMessage.length);
			w.write(serializedMessage);
		}
	}

	protected Message readMessage(BaseServer tcpObserverServer,
			final Socket socket) throws IOException {
		final DataInputStream r = new DataInputStream(socket.getInputStream());
		int length = r.readInt();
		byte[] serializedMessage = new byte[length];
		synchronized (socket) {
			r.readFully(serializedMessage);
		}

		Message m = tcpObserverServer.createMessage(serializedMessage);
		m.origin = this.getEndPointFromSocket(socket);
		return m;
	}

	private EndPoint getEndPointFromSocket(Socket s) {
		String host = s.getInetAddress().getHostAddress();
		int port = s.getPort();
		return new EndPoint(host, port);
	}
}
