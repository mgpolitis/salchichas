package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;

import ar.edu.itba.pod.simul.communication.Transactionable;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.market.Resource;

public class TransactionableRemote implements Transactionable {

	private static final Transactionable INSTANCE = new TransactionableRemote();

	private TransactionableRemote() {
		System.out.println("Creating Transactionable");
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Transactionable get() {
		return INSTANCE;
	}

	private String transactionContextNode = null;
	private Object lock = new Object();

	/**
	 * A transaction context is created between two nodes. Changes between two
	 * nodes can only be done in a transaction context. A node can only be in
	 * one transaction at a time.
	 * <p>
	 * If one node that is in a transaction context tries to create a new one,
	 * an IllegalStateException is thrown. If one that is in a transaction
	 * receives another request, the sender node is blocked until a transaction
	 * context can be created or is ended by time out and an
	 * IllegalStateException is thrown.
	 * 
	 * @param remoteNodeId
	 *            The remote node to establish the transaction context
	 * @param timeOut
	 *            Time limit to wait for a connection
	 * @throws RemoteException
	 */
	public void beginTransaction(String remoteNodeId, long timeout)
			throws RemoteException {
		Preconditions.checkNotNull(remoteNodeId);
		if (transactionContextNode != null) {
			throw new IllegalStateException("A transaction is already running");
		}
		// TODO: do me!
		// a llama al metodo de a con el parametro B
		ConnectionManagerRemote.get().getNodeCommunication().acceptTransaction(
				remoteNodeId);
	}

	/**
	 * Request to create a transaction context to a remote node.
	 * <p>
	 * When a node tries to create a transaction with another node, it must
	 * verify that the remote node can accept the connection and is not been
	 * used in another transaction. This method is used to check this
	 * conditions. As the remote node may already be in a transaction context, a
	 * certain amount of time is used to wait if the remote node ends the
	 * transaction. In case that the node is still blocked by another
	 * transaction, an IllegalStateException is thrown. No guaranty of the order
	 * in which the transactions request are queued is done. If a node is
	 * blocked and two different nodes make a request, when the node is free,
	 * any of the two previous node can be selected to create a new transaction
	 * context.
	 * 
	 * @param remoteNodeId
	 *            The remote node to establish the connection
	 * @throws RemoteException
	 */
	public void acceptTransaction(String remoteNodeId) throws RemoteException {
		// TODO: do me!
	}

	/**
	 * Finalize a transaction context. All the changes done in the context are
	 * persisted, and the two nodes are free for creating new transaction
	 * context.
	 * <p>
	 * If no transaction context exists, an exception is thrown
	 * 
	 * @throws RemoteException
	 */
	public void endTransaction() throws RemoteException {
		// TODO: do me!
	}

	/**
	 * Exchanges a certain amount of a resource between two nodes.
	 * <p>
	 * This method can only be called in a transaction context, otherwise an
	 * IllegalStateException is thrown. The remote node and the destination node
	 * must be different. If not, an IllegalStateException is thrown. The remote
	 * node and the destination node must be the same nodes of the transaction
	 * context, otherwise an IllegalStateException is thrown. Note that the
	 * exchange is not executed but is enqueued. Only when the transaction
	 * context is closed, the changes are applied.
	 * 
	 * @param resource
	 *            The resource being exchanged between the nodes
	 * @param amount
	 *            Positive number of amount being exchanged
	 * @param sourceNode
	 *            The source node of the resource
	 * @param destinationNode
	 *            The destination node of the resource
	 */
	public void exchange(Resource resource, int amount, String sourceNode,
			String destinationNode) throws RemoteException {
		// TODO: do me!
	}

	/**
	 * Creates and returns the payload for the exchange messages.
	 * <p>
	 * This method can only be called in a transaction context, otherwise an
	 * IllegalStateException is thrown. This method can only be called after an
	 * exchange is done, otherwise an IllegalStateException is thrown.
	 * 
	 * @return The payload
	 * @throws RemoteException
	 */
	public Payload getPayload() throws RemoteException {
		// TODO: do me!
		return null;
	}

	/**
	 * Reverts all the changes pending in the transaction context. If this
	 * method is invoked outside a transaction context, an IllegalStateException
	 * is thrown. If an error occurs in the revert, an RuntimeException is
	 * thrown. Otherwise, it is guaranteed that the changes are reverted.
	 */
	public void rollback() throws RemoteException {
		// TODO: do me!
	}

}
