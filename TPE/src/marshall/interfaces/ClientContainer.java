package marshall.interfaces;

import java.io.IOException;

import marshall.base.BaseClient;
import marshall.model.Message;

public interface ClientContainer {

	public void sendMessage(Message m) throws IOException;

	public void subscribeTCPClient(BaseClient client, String serverHost,
			int serverPort) throws IOException;

	public void subscribeUDPClient(BaseClient client, String serverHost,
			int serverPort);

}