package marshall;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientReactor {

	private static final ClientReactor instance = new ClientReactor();
	private static final int backlog = 50;
	private Map<EndPoint,Socket> servers = new HashMap<EndPoint, Socket>();

	BaseClient tcpSenderClient;
	int port;
	
	public static ClientReactor getInstance() {
		return instance;
	}

	private ClientReactor() {

	}

	public void unsubscribeTCPClient() throws IOException{
		Collection<Socket> c = servers.values();
		for( Socket s: c){
			s.close();
		}
		tcpSenderClient = null;
		port = 0;
	}
	
	public void subscribeTCPClient(BaseClient client, int port)
			throws IOException {
		if (tcpSenderClient != null)
			throw new RuntimeException(
					"Can't subscribe more than one TCP client in reactor");

		this.port = port;
		tcpSenderClient = client;
	}

	/* VOID..? Definir qué vuelve... */
	public void useClient(String dhost, int dport,byte[] data) throws IOException{
		EndPoint ep = new EndPoint(dhost,dport);
		Socket client;
		if(servers.containsKey(ep)){
			client = servers.get(ep);
		}
		else{
			client = new Socket(dhost,dport,InetAddress.getLocalHost(),this.port);
			servers.put(ep,client);
		}
		handle(client,data);
	}
	
	/* VOID..? Definir qué vuelve... */
	protected void handle(Socket socket,byte[] data) throws IOException {
		
		final DataInputStream r = new DataInputStream(
				socket.getInputStream());
		
		this.sendMessage(tcpSenderClient.greet());
		
		while (true) {
			Message incomingMessage = this.readMessage(r);
			List<Message> responses = tcpSenderClient.messageReceived(incomingMessage);
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
		Socket socket = servers.get(new EndPoint(m.dest.host, m.dest.port));
		final DataOutputStream w = new DataOutputStream(socket.getOutputStream());
		
		byte [] serializedMessage = m.serialize();
		w.writeInt(serializedMessage.length);
		w.write(serializedMessage);
	}

	protected Message readMessage(DataInputStream r) throws IOException {
		int length = r.readInt();
		byte[] serializedMessage = new byte[length];
		r.readFully(serializedMessage);
		Message m = tcpSenderClient.createMessage(serializedMessage);
		return m;
	}

}
