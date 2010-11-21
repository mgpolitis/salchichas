package ar.edu.itba.pod.legajo49244.dispatcher;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import ar.edu.itba.pod.legajo49244.communication.ClusterAdministrationRemote;
import ar.edu.itba.pod.legajo49244.communication.ConnectionManagerRemote;
import ar.edu.itba.pod.legajo49244.main.Node;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;
import ar.edu.itba.pod.simul.communication.MessageType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.internal.Maps;

public class MessageDispatcher implements MessageListener {

	private static Map<MessageType, Boolean> IS_FORWARDABLE_HELPER = createIsForwardableHelper();

	final DispatcherListener listener;
	final BlockingQueue<Message> ear;

	Map<Message, Long> historyOfBroadcastables;
	Map<String, Long> lastContactedForPull;

	public MessageDispatcher(DispatcherListener listener) {
		System.out.println("Creating Message Listener and Dispatcher");
		this.listener = listener;
		this.ear = new LinkedBlockingQueue<Message>();
		this.historyOfBroadcastables = new ConcurrentHashMap<Message, Long>();
		this.lastContactedForPull = new ConcurrentHashMap<String, Long>();

		new Thread(new DispatcherRunnable()).start();
		new Thread(new MessageForgetterRunnable()).start();
		new Thread(new MessageRequesterRunnable()).start();

		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			Node.exportError(getClass());
		}
	}

	@Override
	public Iterable<Message> getNewMessages(String remoteNodeId)
			throws RemoteException {

		// if I didn't know of nodes existance, add it
		if (ClusterAdministrationRemote.get().getClusterNodes().contains(
				remoteNodeId)) {
			ClusterAdministrationRemote.get().getClusterNodes().add(
					remoteNodeId);
		}

		Long nodeLastContactTimestamp = lastContactedForPull.get(remoteNodeId);
		if (nodeLastContactTimestamp == null) {
			// first time this node is contacting me
			lastContactedForPull.put(remoteNodeId, System.currentTimeMillis());
			return Lists.newArrayList(historyOfBroadcastables.keySet());
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
		Preconditions.checkNotNull(message);

		System.out.println("[Message arrived:]");
		System.out.println("\t- " + message);

		if (message.getNodeId() != null
				&& message.getNodeId().equals(Node.getNodeId())) {
			return false;
		}

		if (historyOfBroadcastables.containsKey(message)) {
			return false;
		}

		this.ear.add(message);

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
					continue;
				}

				MessageType type = message.getType();

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
					break;
				case NODE_MARKET_DATA_REQUEST:
					listener.onNodeMarketDataRequest(message);
					break;
				case RESOURCE_REQUEST:
					listener.onResourceRequest(message);
					break;
				case RESOURCE_TRANSFER:
					// do nothing, deprecated message
					// listener.onResourceTransfer(message);
					break;
				case RESOURCE_TRANSFER_CANCELED:
					// do nothing, deprecated message
					// listener.onResourceTransferCanceled(message);
					break;
				default:
					System.out.println("Unknown message type: " + type);
				}

				// broadcast if neccesary
				if (MessageDispatcher.isForwardable(message)) {
					System.out.println("[Forwarding message: ]");
					System.out.println("\t- " + message);
					try {
						ConnectionManagerRemote.get().getGroupCommunication()
								.broadcast(message);
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
					continue;
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

	private class MessageRequesterRunnable implements Runnable {
		private static final int SLEEP_AMMOUNT_MILLIS = 3000;

		public boolean continueRunning = true;

		@Override
		public void run() {

			while (continueRunning) {

				List<String> nodes = Lists
						.newArrayList(ClusterAdministrationRemote.get()
								.getClusterNodes());
				Collections.shuffle(nodes);
				if (nodes.size() > 0) {

					String randomNode = nodes.get(0);
					try {
						ConnectionManagerRemote.get().getConnectionManager(
								randomNode).getGroupCommunication()
								.getListener().getNewMessages(Node.getNodeId());
						Iterable<Message> messages = Lists.newArrayList();

						for (Message m : messages) {
							MessageDispatcher.this.onMessageArrive(m);
						}
					} catch (RemoteException e) {
						// don't sleep, immediately choose other node to get
						// messages from
						continue;
					}
					System.out.println("Requesting new messages from "
							+ randomNode);
				} else {
					System.out.println("No peers to request messages from.");
				}

				try {
					Thread.sleep(SLEEP_AMMOUNT_MILLIS);
				} catch (InterruptedException e) {
					System.out.println("thread interrupted while sleeping?");
					e.printStackTrace();
				}

			}
		}
	}

	private static Map<MessageType, Boolean> createIsForwardableHelper() {
		Map<MessageType, Boolean> ret = Maps.newHashMap();
		List<MessageType> forwardables = Lists.newArrayList(
				MessageType.DISCONNECT, MessageType.RESOURCE_REQUEST,
				MessageType.NODE_AGENTS_LOAD_REQUEST);
		List<MessageType> nonForwardables = Lists.newArrayList(
				MessageType.NODE_AGENTS_LOAD, MessageType.NODE_MARKET_DATA,
				MessageType.NODE_MARKET_DATA_REQUEST,
				MessageType.RESOURCE_TRANSFER,
				MessageType.RESOURCE_TRANSFER_CANCELED);
		for (MessageType type : MessageType.values()) {
			if (forwardables.contains(type)) {
				ret.put(type, true);
			} else if (nonForwardables.contains(type)) {
				ret.put(type, false);
			} else {
				throw new IllegalStateException(
						"All messages must be either forwardable or not forwardable");
			}
		}
		return ret;
	}

	private static boolean isForwardable(Message message) {
		Preconditions.checkNotNull(message);
		MessageType type = message.getType();
		Boolean ret = IS_FORWARDABLE_HELPER.get(type);
		if (ret == null) {
			throw new IllegalArgumentException("I don't message type " + type);
		}
		return ret;
	}

}
