package ar.edu.itba.pod.legajo49244.dispatcher;

import ar.edu.itba.pod.simul.communication.Message;

public interface DispatcherListener {

	public boolean onNodeAgentsLoad(Message message);

	public boolean onNodeAgentsLoadRequest(Message message);

	public boolean onNodeMarketData(Message message);

	public boolean onNodeMarketDataRequest(Message message);

	public boolean onDisconnect(Message message);

	public boolean onResourceRequest(Message message);

}
