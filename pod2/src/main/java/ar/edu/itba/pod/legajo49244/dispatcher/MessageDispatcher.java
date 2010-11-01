package ar.edu.itba.pod.legajo49244.dispatcher;

import java.rmi.RemoteException;

import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class MessageDispatcher implements MessageListener {

	@Override
	public Iterable<Message> getNewMessages(String remoteNodeId)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onMessageArrive(Message message) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

}
