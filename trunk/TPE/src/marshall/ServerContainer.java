package marshall;

import java.io.IOException;

import marshall.interfaces.BaseServer;

public interface ServerContainer {

	public abstract void subscribeTCPServer(BaseServer server, int listenPort)
			throws IOException;

	public abstract void subscribeUDPServer(BaseServer server, int listenPort)
			throws IOException;

}