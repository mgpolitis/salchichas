package ar.edu.itba.pod.legajo49244.dispatcher;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ar.edu.itba.pod.legajo49244.communication.ConnectionManagerRemote;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;
import ar.edu.itba.pod.simul.communication.MessageType;

import com.google.common.collect.Lists;
import com.google.inject.internal.Maps;

public class MessageDispatcher implements MessageListener {

	private static final Map<MessageType, Boolean> IS_FORWARDABLE_HELPER = createIsForwardableHelper();

	final SimulationListener listener;
	final BlockingQueue<Message> ear;

	Map<Message, Long> historyOfBroadcastables;
	Map<String, Long> lastContactedForPull;

	public MessageDispatcher(SimulationListener listener) {
		this.listener = listener;
		this.ear = new LinkedBlockingQueue<Message>();
		this.historyOfBroadcastables = new HashMap<Message, Long>();
		this.lastContactedForPull = new HashMap<String, Long>();

		new Thread(new DispatcherRunnable()).start();
		new Thread(new MessageForgetterRunnable()).start();
	}

	@Override
	public Iterable<Message> getNewMessages(String remoteNodeId)
			throws RemoteException {
		Long nodeLastContactTimestamp = lastContactedForPull.get(remoteNodeId);
		if (nodeLastContactTimestamp == null) {
			// first time this node is contacting me
			lastContactedForPull.put(remoteNodeId, System.currentTimeMillis());
			return historyOfBroadcastables.keySet();
		}

		// this node contacted me in the past
		List<Message> ret = Lists.newArrayList();
		for (Entry<Message, Long> entry : this.historyOfBroadcastables
				.entrySet()) {
			Long messageTimeStamp = entry.getValue();
			if (nodeLastContactTimestamp < messageTimeStamp) {
				ret.add(entry.getKey());
			}
		}
		return ret;
	}

	@Override
	public boolean onMessageArrive(Message message) throws RemoteException {

		if (historyOfBroadcastables.containsKey(message)) {
			return false;
		}

		if (isForwardable(message)) {
			historyOfBroadcastables.put(message, System.currentTimeMillis());
		}

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
				System.out.println("Message read!!!");

				// let delegate process message

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
				/*case RESOURCE_TRANSFER_CANCELED:
					listener.onResourceTransferCanceled(message);
					break;*/
				default:
					throw new IllegalStateException("Unknown message type: "
							+ type);
				}

				// broadcast if neccesary
				if (MessageDispatcher.isForwardable(message)) {
					try {
						ConnectionManagerRemote.getInstance()
								.getGroupCommunication().broadcast(message);
					} catch (RemoteException e) {
						System.out
								.println("Broadcast failed, will wait for pull to fix it");
					}
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
				for (Message m : historyOfBroadcastables.keySet()) {
					long timeStamp = historyOfBroadcastables.get(m);
					if (timeStamp < now - OBSOLETE_AMMOUNT_MILLIS) {
						// old message, forget it
						historyOfBroadcastables.remove(m);
					}
				}
			}
		}

	}

	private static Map<MessageType, Boolean> createIsForwardableHelper() {
		Map<MessageType, Boolean> ret = Maps.newHashMap();
		List<MessageType> forwardables = Lists.newArrayList(
				MessageType.DISCONNECT, MessageType.NODE_AGENTS_LOAD,
				MessageType.RESOURCE_REQUEST);
		List<MessageType> nonForwardables = Lists.newArrayList(
				MessageType.NODE_AGENTS_LOAD,
				MessageType.NODE_AGENTS_LOAD_REQUEST,
				MessageType.NODE_MARKET_DATA,
				MessageType.NODE_MARKET_DATA_REQUEST,
				MessageType.RESOURCE_TRANSFER);
		for (MessageType type : MessageType.values()) {
			if (forwardables.contains(type)) {
				ret.put(type, true);
			} else if (!nonForwardables.contains(type)) {
				throw new IllegalStateException(
						"All messages must be either forwardable or not forwardable");
			}
		}
		return ret;
	}

	private static boolean isForwardable(Message message) {
		return IS_FORWARDABLE_HELPER.get(message.getType());
	}

}
