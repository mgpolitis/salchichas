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

import marshall.interfaces.BaseServer;
import marshall.model.EndPoint;
import marshall.model.Message;

public class ServerReactor {

	private static final ServerReactor instance = new ServerReactor();
	private static final int backlog = 50;
	private Map<EndPoint, Socket> clients = new HashMap<EndPoint, Socket>();

	BaseServer tcpObserverServer;
	ServerSocket serverSocket;

	public static ServerReactor getInstance() {
		return instance;
	}

	private ServerReactor() {

	}

	public void subscribeTCPServer(BaseServer server, int port)
			throws IOException {
		if (serverSocket != null || tcpObserverServer != null)
			throw new RuntimeException(
					"Can't subscribe more than one TCP server in reactor");

		serverSocket = new ServerSocket(port, ServerReactor.backlog,
				InetAddress.getByName("localhost"));
		tcpObserverServer = server;
	}

	public void runServer() throws IOException {
		final ServerReactor thiz = this;
		System.out.println("Starting server "
				+ this.tcpObserverServer.getClass().getName() + " on "
				+ this.serverSocket.getLocalSocketAddress().toString() + ".");
		System.out.println("Now accepting clients...");
		while (true) {
			final Socket socket = serverSocket.accept();

			EndPoint newClientEndPoint = this.getEndPointFromSocket(socket);
			clients.put(newClientEndPoint, socket);

			new Thread(new Runnable() {
				public void run() {
					try {
						String s = socket.getRemoteSocketAddress().toString();
						System.out.printf("Client has connected: %s\n", s);
						thiz.handle(socket);
						if (!socket.isClosed()) {
							socket.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}).start();

		}
	}

	protected void handle(Socket socket) throws IOException {

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

		return;

	}

	private void sendMessage(Message m) throws IOException {
		Socket socket = clients.get(new EndPoint(m.dest.host, m.dest.port));
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
