package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.List;

import ar.edu.itba.pod.legajo49244.dispatcher.MessageDispatcher;
import ar.edu.itba.pod.legajo49244.simulation.DistributedSimulationManager;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

import com.google.common.collect.Lists;

public class ClusterCommunicationRemote implements ClusterCommunication {

	private static final int BROADCAST_AMMOUNT = 3;

	private static final ClusterCommunicationRemote INSTANCE = new ClusterCommunicationRemote();
	private MessageListener messageListener;
	
	public static ClusterCommunicationRemote get() {
		return INSTANCE;
	}

	private ClusterCommunicationRemote() {
		System.out.println("Creating ClusterCommunication");
		DistributedSimulationManager distributedSimulationManager = DistributedSimulationManager.get();
		this.messageListener = new MessageDispatcher(distributedSimulationManager);
		try {
			UnicastRemoteObject.exportObject(this,0);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public MessageListener getListener() throws RemoteException {
		return messageListener;
	}

	@Override
	public void broadcast(Message message) throws RemoteException {
		List<String> nodes = Lists.newArrayList(ClusterAdministrationRemote
				.get().getClusterNodes());
		Collections.shuffle(nodes);
		int limit = nodes.size();
		if (nodes.size() >= BROADCAST_AMMOUNT) {
			limit = BROADCAST_AMMOUNT;
		}
		for (int i = 0; i < limit; i++) {
			String node = nodes.get(i);
			try {
				if (!this.send(message, node)) {
					break;
				}
			} catch (RemoteException e) {
				// could not send message to that node
				System.out.println("could not broadcast to node "+node+", he was down.");
			}
		}

	}

	@Override
	public boolean send(Message message, String nodeId) throws RemoteException {
		
		ConnectionManager otherManager = ConnectionManagerRemote.get()
				.getConnectionManager(nodeId);
		System.out.println("[Sending message to "+nodeId+":]");
		System.out.println("\t- "+message);
		boolean ret = otherManager.getGroupCommunication().getListener()
		.onMessageArrive(message);
		System.out.println("[message sent]");
		return ret;
	}
}
