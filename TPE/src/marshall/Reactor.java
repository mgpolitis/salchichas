package marshall;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class Reactor {

	private static final Reactor instance = new Reactor();
	private static final int backlog = 50;
	
	private Map<Integer, BaseServer> tcpObserverServers = new HashMap<Integer, BaseServer>();
	private Map<Integer, BaseServer> tcpObserverClients = new HashMap<Integer, BaseServer>();
	
	private Map<Integer, ServerSocket> tcpPool = new HashMap<Integer, ServerSocket>();
	
	public static Reactor getInstance() {
		return instance;
	}
	
	
	private Reactor() {
		
	}
	
	public void subscribeTCPServer(BaseServer server, int port) throws IOException {
		ServerSocket s = new ServerSocket(port, Reactor.backlog);
		tcpPool.put(8083, s);
	}
	
}
