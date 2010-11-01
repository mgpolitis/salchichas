package ar.edu.itba.pod.legajo49244.communication;

import java.io.Serializable;
import java.rmi.RemoteException;

import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class ClusterCommunicationRemote implements Serializable,
		ClusterCommunication {

	private static final ClusterCommunication INSTANCE = new ClusterCommunicationRemote();

	public static ClusterCommunication getInstance() {
		return INSTANCE;
	}
	
	private ClusterCommunicationRemote() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void broadcast(Message message) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public MessageListener getListener() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean send(Message message, String nodeId) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

}
