package marshall;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Reactor {

	private static final Reactor instance = new Reactor();
	private static final int backlog = 50;

	BaseServer tcpObserverServer;
	ServerSocket serverSocket;

	public static Reactor getInstance() {
		return instance;
	}

	private Reactor() {

	}

	public void subscribeTCPServer(BaseServer server, int port)
			throws IOException {
		if (serverSocket != null || tcpObserverServer != null)
			throw new RuntimeException(
					"Can't subscribe more than one TCP server in reactor");

		serverSocket = new ServerSocket(port, Reactor.backlog, InetAddress
				.getLocalHost());
		tcpObserverServer = server;
	}

	public void run() throws IOException {
		final Reactor thiz = this;
		while (true) {
			final Socket socket = serverSocket.accept();
			new Thread(new Runnable() {
				public void run() {
					try {
						String s = socket.getRemoteSocketAddress().toString();
						System.out.printf("Se conecto %s\n", s);
						thiz.handle(socket);
						if (!socket.isClosed()) {
							socket.close();
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}).start();

		}
	}

	protected void handle(Socket socket) throws IOException {
		final DataOutputStream w = new DataOutputStream(socket.getOutputStream());
		final DataInputStream r = new DataInputStream(
				socket.getInputStream());
		
		Message gm = this.readMessage(r);
		for (Message m : tcpObserverServer.messageReceived(gm)) {
			this.sendMessage(m, w);
		}
		
		
		// usar DataOutput!!!

	}
	
	private void sendMessage(Message m, DataOutputStream w) throws IOException {
		byte [] serializedMessage = m.serialize();
		w.writeInt(serializedMessage.length);
		w.write(serializedMessage);
	}

	protected Message readMessage(DataInputStream r) throws IOException {
		int length = r.readInt();
		byte[] serializedMessage = new byte[length];
		r.readFully(serializedMessage);
		Message m = tcpObserverServer.createMessage(serializedMessage);
		return m;
	}

}
