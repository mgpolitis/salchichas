package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import ar.edu.itba.pod.legajo49244.Node;
import ar.edu.itba.pod.legajo49244.communication.payload.Payloads;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;

import com.google.common.collect.Lists;

public class ClusterAdministrationRemote implements ClusterAdministration {

	private static final ClusterAdministrationRemote INSTANCE = new ClusterAdministrationRemote();
	private static final String DEFAULT_CLUSTER_NAME = "eva";

	private Set<String> clusterNodes = new HashSet<String>();

	private String clusterName = null;
	private boolean isConnected = false;
	private ConnectionManager connectionManager = ConnectionManagerRemote
			.getInstance();

	private ClusterAdministrationRemote() {
	}

	public static ClusterAdministrationRemote getInstance() {
		return INSTANCE;
	}

	@Override
	public void createGroup() throws RemoteException {
		if (clusterName != null) {
			throw new IllegalArgumentException();
		}
		clusterName = DEFAULT_CLUSTER_NAME;
		isConnected = true;
	}

	@Override
	public boolean isConnectedToGroup() throws RemoteException {
		return isConnected;
	}

	@Override
	public String getGroupId() throws RemoteException {
		return clusterName;
	}

	@Override
	public void connectToGroup(String initialNode) throws RemoteException {
		if (initialNode.equals(Node.NODE_ID)) {
			throw new IllegalArgumentException(
					"Cannot connect to cluster through yourself, node!");
		}
		if (this.isConnectedToGroup()) {
			throw new IllegalStateException(
					"Cannot reconnect, node is already connected to a cluster");
		}

		ConnectionManager initialCM = connectionManager
				.getConnectionManager(initialNode);

		clusterName = initialCM.getClusterAdmimnistration().getGroupId();

		Iterable<String> nodes = initialCM.getClusterAdmimnistration()
				.addNewNode(Node.NODE_ID);
		for (String node : nodes) {
			clusterNodes.add(node);
		}
		isConnected = true;

	}

	@Override
	public Iterable<String> addNewNode(String newNode) throws RemoteException {
		if (!this.isConnectedToGroup()) {
			throw new IllegalStateException("Node " + Node.NODE_ID
					+ " is not connected to a cluster.");
		}
		ConnectionManager newNodeCM = connectionManager
				.getConnectionManager(newNode);
		if (!this.getGroupId().equals(
				newNodeCM.getClusterAdmimnistration().getGroupId())) {
			throw new IllegalArgumentException(
					"Must belong to the same group to connect");
		}

		if (this.clusterNodes.contains(newNode)) {
			// I already knew this node, ignore
			return Lists.newArrayList();
		}

		Set<String> ret = new HashSet<String>();
		ret.addAll(this.clusterNodes);

		this.clusterNodes.add(newNode);

		return ret;
	}

	/**
	 * A broadcast message is created informing the nodes of the cluster that
	 * the node sent by parameter has left the cluster.
	 * <p>
	 * The sender node sends the message to N random nodes. In each iteration,
	 * the node is informed if this message is new or not. This information is
	 * used to determine whether to continue or not with the broadcast. The
	 * sender node can be the same node that is been disconnected. If the node
	 * is already disconnected, an IllegalArgumentException is thrown.
	 */
	@Override
	public void disconnectFromGroup(String nodeId) throws RemoteException {
		if (!isConnected) {
			throw new IllegalArgumentException("El nodo no estaba conectado.");
		}

		connectionManager.getGroupCommunication().broadcast(
				new Message(Node.getNodeId(), System.currentTimeMillis(),
						MessageType.DISCONNECT, Payloads
								.newDisconnectPayload(nodeId)));

	}

	public Set<String> getClusterNodes() {
		return clusterNodes;
	}

}
