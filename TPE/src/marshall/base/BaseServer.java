package marshall.base;

import java.io.IOException;
import java.util.List;

import marshall.interfaces.ServerContainer;
import marshall.model.Message;

public abstract class BaseServer {

	private ServerContainer container;

	public void sendMessage(Message m) throws IOException {
		if (container == null) {
			throw new IllegalStateException(
					"container not initialized for BaseClient " + toString());
		}
		container.sendMessage(m);
	}

	public void setContainer(ServerContainer container) {
		this.container = container;
	}

	public abstract List<Message> messageReceived(Message m);

	public abstract Message createMessage(byte[] serialized);

}
