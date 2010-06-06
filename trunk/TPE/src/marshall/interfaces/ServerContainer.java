package marshall.interfaces;

import java.io.IOException;

import marshall.base.BaseServer;
import marshall.model.Message;

public interface ServerContainer {

	public void sendMessage(Message m) throws IOException;

	public void subscribeTCPServer(BaseServer server, int listenPort)
			throws IOException;

	public void subscribeUDPServer(BaseServer server, int listenPort)
			throws IOException;

}