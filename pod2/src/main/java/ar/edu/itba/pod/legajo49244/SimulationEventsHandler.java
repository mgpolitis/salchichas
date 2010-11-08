package ar.edu.itba.pod.legajo49244;

import ar.edu.itba.pod.legajo49244.dispatcher.DispatcherListener;
import ar.edu.itba.pod.simul.communication.Message;

public class SimulationEventsHandler implements DispatcherListener {

	@Override
	public boolean onDisconnect(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onNodeAgentsLoad(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onNodeAgentsLoadRequest(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onNodeMarketData(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onNodeMarketDataRequest(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onResourceRequest(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onResourceTransfer(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onResourceTransferCanceled(Message message) {
		// TODO Auto-generated method stub
		return false;
	}


}
