package ar.edu.itba.pod.legajo49244.dispatcher;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;
import ar.edu.itba.pod.simul.communication.MessageType;

public class MessageDispatcher implements MessageListener {

	final SimulationListener listener;
	final BlockingQueue<Message> ear;

	Map<Message, Long> history;
	Map<String, Long> lastContacted;

	public MessageDispatcher(SimulationListener listener) {
		this.listener = listener;
		this.ear = new LinkedBlockingQueue<Message>();
		this.history = new HashMap<Message, Long>();
		this.lastContacted = new HashMap<String, Long>();

		new Thread(new DispatcherRunnable()).start();
		new Thread(new MessageForgetterRunnable()).start();
	}

	@Override
	public Iterable<Message> getNewMessages(String remoteNodeId)
			throws RemoteException {

		return null;
	}

	@Override
	public boolean onMessageArrive(Message message) throws RemoteException {

		if (history.containsKey(message)) {
			return false;
		}

		if (this.isForwardable(message)) {
			// TODO: preguntar esto de forwardable
			history.put(message, System.currentTimeMillis());
		}
		
		// TODO: que retorno si no es forwardable y lo recibo 2 veces?
		return true;

	}

	private class DispatcherRunnable implements Runnable {

		public boolean continueRunning = true;

		@Override
		public void run() {
			while (continueRunning) {
				Message message = null;
				try {
					message = ear.take();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				MessageType type = message.getType();

				switch (type) {
				case DISCONNECT:
					listener.onDisconnect(message);
					break;
				case NODE_AGENTS_LOAD:
					listener.onNodeAgentsLoad(message);
					break;
				case NODE_AGENTS_LOAD_REQUEST:
					listener.onNodeAgentsLoadRequest(message);
					break;
				case NODE_MARKET_DATA:
					listener.onNodeMarketData(message);
				case NODE_MARKET_DATA_REQUEST:
					listener.onNodeMarketDataRequest(message);
					break;
				case RESOURCE_REQUEST:
					listener.onResourceRequest(message);
					break;
				case RESOURCE_TRANSFER:
					listener.onResourceTransfer(message);
					break;
				case RESOURCE_TRANSFER_CANCELED:
					listener.onResourceTransferCanceled(message);
					break;
				default:
					throw new IllegalStateException("Unknown message type: "
							+ type);
				}

			}

		}

	}

	private class MessageForgetterRunnable implements Runnable {

		private static final int SLEEP_AMMOUNT_MILLIS = 3000;
		private static final int OBSOLETE_AMMOUNT_MILLIS = 3000;

		public boolean continueRunning = true;

		@Override
		public void run() {
			while (continueRunning) {
				try {
					Thread.sleep(SLEEP_AMMOUNT_MILLIS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long now = System.currentTimeMillis();
				for (Message m : history.keySet()) {
					long timeStamp = history.get(m);
					if (timeStamp < now - OBSOLETE_AMMOUNT_MILLIS) {
						// old message, forget it
						history.remove(m);
					}
				}
			}

		}

	}

	private boolean isForwardable(Message message) {
		return true;
	}

}
