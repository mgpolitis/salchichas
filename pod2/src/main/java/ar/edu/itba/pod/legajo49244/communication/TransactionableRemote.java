package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ar.edu.itba.pod.simul.communication.Transactionable;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.market.Resource;

public class TransactionableRemote implements Transactionable {

	private static final Transactionable INSTANCE = new TransactionableRemote();

	private TransactionableRemote() {
		System.out.println("Creating Transactionable");
		try {
			UnicastRemoteObject.exportObject(this,0);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Transactionable get() {
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
