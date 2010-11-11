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

	private static final ClusterCommunication INSTANCE = new ClusterCommunicationRemote();
	private MessageListener messageListener;
	
	private DistributedSimulationManager distributedSimulationManager;

	public DistributedSimulationManager getDistributedSimulationManager() {
		return distributedSimulationManager;
	}

	public static ClusterCommunication getInstance() {
		return INSTANCE;
	}

	private ClusterCommunicationRemote() {
		System.out.println("Creating ClusterCommunication");
		distributedSimulationManager = new DistributedSimulationManager();
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
				.getInstance().getClusterNodes());
		Collections.shuffle(nodes);
		int limit = nodes.size();
		if (nodes.size() >= BROADCAST_AMMOUNT) {
			limit = BROADCAST_AMMOUNT;
		}
		for (int i = 0; i < limit; i++) {
			String node = nodes.get(i);
			if (!this.send(message, node)) {
				break;
			}
		}

	}

	@Override
	public boolean send(Message message, String nodeId) throws RemoteException {
		ConnectionManager otherManager = ConnectionManagerRemote.getInstance()
				.getConnectionManager(nodeId);
		return otherManager.getGroupCommunication().getListener()
				.onMessageArrive(message);
	}
}
