package ar.edu.itba.pod.legajo49244.communication;

import java.io.Serializable;
import java.rmi.RemoteException;

import ar.edu.itba.pod.simul.communication.Transactionable;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.market.Resource;

public class TransactionableRemote implements Transactionable, Serializable {

	private static final Transactionable INSTANCE = new TransactionableRemote();
	
	private TransactionableRemote() {
		// TODO: method stub
	}
	
	public static Transactionable getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void acceptTransaction(String remoteNodeId) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void beginTransaction(String remoteNodeId, long timeout)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endTransaction() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void exchange(Resource resource, int amount, String sourceNode,
			String destinationNode) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public Payload getPayload() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rollback() throws RemoteException {
		// TODO Auto-generated method stub

	}

}
