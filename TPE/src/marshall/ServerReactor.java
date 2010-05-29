package marshall;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

public class ServerReactor implements ServerContainer {

	private static final ServerReactor instance = new ServerReactor();
	private static final int BACKLOG = 50;
	private static final int THREADS_IN_POOL = 10;
	private Map<EndPoint, Socket> clients = new HashMap<EndPoint, Socket>();

	BaseServer tcpObserverServer;
	ServerSocket serverSocket;

	public static ServerReactor getInstance() {
		return instance;
	}

	private ServerReactor() {
	}

	private boolean initialized() {
		return (serverSocket != null || tcpObserverServer != null);
	}

	public void subscribeTCPServer(BaseServer server, int listenPort)
			throws IOException {
		if (this.initialized())
			throw new RuntimeException(
					"Can't subscribe more than one TCP server in reactor");

		serverSocket = new ServerSocket(listenPort, ServerReactor.BACKLOG,
				InetAddress.getByName("localhost"));
		tcpObserverServer = server;
	}

	public void subscribeUDPServer(BaseServer server, int listenPort)
			throws IOException {
		throw new IllegalArgumentException("Method not yet implemented");
	}

	public void runServer() {
		final ServerReactor thiz = this;

		if (!this.initialized()) {
			System.out.println("Starting no servers. (none subscribed)");
			return;
		}

		System.out.println("Starting server "
				+ this.tcpObserverServer.getClass().getName() + " on "
				+ this.serverSocket.getLocalSocketAddress().toString() + ".");
		System.out.println("Now accepting clients...");
		final ExecutorService es = Executors
				.newFixedThreadPool(THREADS_IN_POOL);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					final Socket socket;
					try {
						socket = serverSocket.accept();
					} catch (IOException e1) {
						e1.printStackTrace();
						return;
					}
					final EndPoint newClientEndPoint = thiz
							.getEndPointFromSocket(socket);
					clients.put(newClientEndPoint, socket);

					Runnable runner = new Runnable() {
						public void run() {
							try {
								System.out.printf("Client has connected: %s\n",
										socket.getRemoteSocketAddress()
												.toString());
								thiz.handle(newClientEndPoint);
								if (!socket.isClosed()) {
									socket.close();
								}
							} catch (IOException e) {
								e.printStackTrace();
							} finally {
								clients.remove(newClientEndPoint);
							}

						}
					};
					es.execute(runner);

				}
			}
		}).start();

	}

	protected void handle(EndPoint clientEndPoint) throws IOException {
		Socket socket = clients.get(clientEndPoint);
		while (true) {
			Message incomingMessage = this.readMessage(socket);
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

	private void sendMessage(Message m) throws IOException {
		Socket socket = clients.get(new EndPoint(m.dest.host, m.dest.port));
		if (socket == null) {
			throw new IOException(
					"ERROR sending message: Endpoint not connected");
		}
		final DataOutputStream w = new DataOutputStream(socket
				.getOutputStream());

		byte[] serializedMessage = m.serialize();
		w.writeInt(serializedMessage.length);
		w.write(serializedMessage);
	}

	protected Message readMessage(Socket socket) throws IOException {
		final DataInputStream r = new DataInputStream(socket.getInputStream());
		int length = r.readInt();
		byte[] serializedMessage = new byte[length];
		r.readFully(serializedMessage);
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
