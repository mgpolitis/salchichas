package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ar.edu.itba.pod.legajo49244.communication.payload.Payloads;
import ar.edu.itba.pod.legajo49244.main.Node;
import ar.edu.itba.pod.simul.communication.Transactionable;
import ar.edu.itba.pod.simul.communication.payload.Payload;
import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;
import ar.edu.itba.pod.simul.market.Resource;

import com.google.common.base.Preconditions;

public class TransactionableRemote implements Transactionable {

	private static final TransactionableRemote INSTANCE = new TransactionableRemote();
	private static final long ACCEPT_SLEEP_TIME = 1000;

	private TransactionableRemote() {
		System.out.println("Creating Transactionable");
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			Node.exportError(this.getClass());
		}
	}

	public static TransactionableRemote get() {
		return INSTANCE;
	}

	private String transactionContextNode = null;
	private ResourceTransferMessagePayload transactionPayload = null;
	private Long transactionTimeout = null;

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

		System.out.println("*****Begin transaction called");

		// Hace sus validaciones correspondientes
		Preconditions.checkNotNull(remoteNodeId);
		if (transactionContextNode != null) {
			throw new IllegalStateException("A transaction is already running");
		}

		// Invoca el acceptTransaction del nodo B
		try {
			ConnectionManagerRemote.get().getConnectionManager(remoteNodeId)
					.getNodeCommunication().acceptTransaction(Node.getNodeId());
		} catch (Exception e) {
			// Si lanza una exception, considera que no se pudo hacer la
			// transacción
			System.out.println("Could not create transaction, abort.");
			throw new IllegalStateException();
		}

		// Si retorna bien, considera la transacción creada
		transactionContextNode = remoteNodeId;
		transactionTimeout = timeout;
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

		System.out.println("*****Accept called");

		// Si no esta en una transacción, retorna
		if (transactionContextNode == null) {
			transactionContextNode = remoteNodeId;
			return;
		}

		// Si ya estaba en una transacción, hace un sleep de un cierto tiempo.
		try {
			Thread.sleep(ACCEPT_SLEEP_TIME);
		} catch (InterruptedException e) {
			// do nothing, just cancel waiting
		}
		// Al salir del sleep verifica si sigue en la transacción
		// Si continua en la transacción, lanza una excepción
		if (transactionContextNode != null) {
			throw new IllegalStateException(
					"Could not obtain transaction priviledges");
		} else {
			// Si no se encuentra en una transacción retorna
			return;
		}

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

		System.out.println("*****exchange called");

		if (transactionContextNode == null) {
			throw new IllegalStateException(
					"cannot exchange without a transaction context");
		}
		if (sourceNode.equals(destinationNode)) {
			throw new IllegalStateException(
					"source and dest nodes must be different");
		}
		if (!sourceNode.equals(transactionContextNode)
				&& !destinationNode.equals(transactionContextNode)) {
			throw new IllegalStateException(
					"Transaction nodes must be the same as the transaction context");
		}

		transactionPayload = Payloads.newResourceTransferMessagePayload(
				sourceNode, destinationNode, resource, amount);

		return;
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
		Preconditions.checkState(transactionContextNode != null,
				"Must have a transaction context!");
		Preconditions.checkState(transactionPayload != null,
				"Must have made an exchange before calling this!");
		return transactionPayload;
	}

	/**
	 * Finalize a transaction context. All the changes done in the context are
	 * persisted, and the two nodes are free for creating new transaction
	 * context.
	 * 
	 */
	public void endTransaction() throws RemoteException {
		System.out.println("****endTransaction Called");
		Preconditions.checkState(transactionContextNode != null,
				"A transaction context must exist");

		if (transactionPayload.getDestination().equals(Node.getNodeId())) {
			// if I am not coordinator, clean context and end
			cleanContext();
			return;
		}

		// Luego, el nodo B invoca el endTransaction.
		// Este método, asume que el nodo B es el coordinador.

		// El coordinador invoca el canCommit en ambos nodos.
		boolean otherOK = false;
		try {
			otherOK = ConnectionManagerRemote.get().getConnectionManager(
					transactionContextNode).getThreePhaseCommit().canCommit(
					Node.getNodeId(), transactionTimeout);
		} catch (RemoteException e) {
		}
		boolean meOK = ThreePhaseCommitRemote.get().canCommit(Node.getNodeId(),
				transactionTimeout);

		if (!otherOK || !meOK) {
			this.rollback();
			return;
		}

		// El coordinador invoca el preCommit en ambos nodos.
		otherOK = false;
		try {
			ConnectionManagerRemote.get().getConnectionManager(
					transactionContextNode).getThreePhaseCommit().preCommit(
					Node.getNodeId());
			otherOK = true;
		} catch (RemoteException e) {
		}
		ThreePhaseCommitRemote.get().preCommit((Node.getNodeId()));

		if (!otherOK || !meOK) {
			this.rollback();
			return;
		}

		// El coordinador invoca el doCommit en ambos nodos.
		try {
			ConnectionManagerRemote.get().getConnectionManager(
					transactionContextNode).getThreePhaseCommit().doCommit(
					Node.getNodeId());
		} catch (RemoteException e) {
			// no importa, la transaccion se realizo
			System.out.println("!!!!! remote exception en doCommit!!!!!!!!");
		}
		ThreePhaseCommitRemote.get().doCommit((Node.getNodeId()));

	}

	/**
	 * Reverts all the changes pending in the transaction context. If this
	 * method is invoked outside a transaction context, an IllegalStateException
	 * is thrown. If an error occurs in the revert, an RuntimeException is
	 * thrown. Otherwise, it is guaranteed that the changes are reverted.
	 */
	public void rollback() throws RemoteException {
		System.out.println("****rollback Called");
		Preconditions.checkState(transactionContextNode != null,
				"A transaction context must exist");
		cleanContext();

	}

	public void cleanContext() {
		transactionTimeout = null;
		transactionPayload = null;
		transactionContextNode = null;
	}

}
