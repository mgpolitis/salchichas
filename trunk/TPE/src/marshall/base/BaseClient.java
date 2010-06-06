package marshall.base;

import java.io.IOException;
import java.util.List;

import marshall.interfaces.ClientContainer;
import marshall.model.Message;

public abstract class BaseClient {

	private ClientContainer container;

	protected void sendMessage(Message m) throws IOException {
		if (container == null) {
			throw new IllegalStateException(
					"container not initialized for BaseClient " + toString());
		}
		container.sendMessage(m);
	}

	public void setContainer(ClientContainer container) {
		this.container = container;
	}

	public abstract List<Message> messageReceived(Message m);

	public abstract Message greet();

	public abstract Message createMessage(byte[] serialized);

}
