package marshall;

import java.io.IOException;

import marshall.base.BaseClient;

public interface ClientContainer {

	public abstract void subscribeTCPClient(BaseClient client,
			String serverHost, int serverPort) throws IOException;

	public abstract void subscribeUDPClient(BaseClient client,
			String serverHost, int serverPort);

}