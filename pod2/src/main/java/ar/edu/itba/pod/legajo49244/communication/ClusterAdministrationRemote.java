package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.edu.itba.pod.legajo49244.communication.payload.Payloads;
import ar.edu.itba.pod.legajo49244.main.Node;
import ar.edu.itba.pod.legajo49244.message.Messages;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ConnectionManager;

import com.google.common.collect.Lists;

public class ClusterAdministrationRemote implements ClusterAdministration {

	private static final ClusterAdministrationRemote INSTANCE = new ClusterAdministrationRemote();
	private static final String DEFAULT_CLUSTER_NAME = "eva";

	private Set<String> clusterNodes = new HashSet<String>();

	private String clusterName = null;
	private boolean isConnected = false;
	private ConnectionManager connectionManager = ConnectionManagerRemote.get();

	private ClusterAdministrationRemote() {
		System.out.println("Creating ClusterAdministration");
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ClusterAdministrationRemote get() {
		return INSTANCE;
	}

	@Override
	public void createGroup() throws RemoteException {
		if (clusterName != null) {
			throw new IllegalArgumentException();
		}
		clusterName = DEFAULT_CLUSTER_NAME;
		isConnected = true;
		System.out.println("group " + clusterName + " created");
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
		if (initialNode.equals(Node.getNodeId())) {
			throw new IllegalArgumentException(
					"Cannot connect to cluster through yourself, node!");
		}
		if (this.isConnectedToGroup()) {
			throw new IllegalStateException(
					"Cannot reconnect, node is already connected to a cluster");
		}

		System.out.println("connecting to group with node " + initialNode);

		ConnectionManager initialCM = connectionManager
				.getConnectionManager(initialNode);

		clusterName = initialCM.getClusterAdmimnistration().getGroupId();
		System.out.println("Connected to group with name " + clusterName);

		isConnected = true;
		Iterable<String> nodes = initialCM.getClusterAdmimnistration()
				.addNewNode(Node.getNodeId());
		for (String node : nodes) {
			clusterNodes.add(node);
		}
		clusterNodes.add(initialNode);

	}

	@Override
	public Iterable<String> addNewNode(String newNode) throws RemoteException {
		if (Node.getNodeId().equals(newNode)) {
			return Lists.newArrayList();
		}
		if (!this.isConnectedToGroup()) {
			throw new IllegalStateException("Node " + Node.getNodeId()
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
		System.out.println("Adding new node: " + newNode + " (count: "
				+ (this.clusterNodes.size()+1) + ")");
		
		Set<String> ret = new HashSet<String>();
		ret.addAll(this.clusterNodes);
		ret.add(Node.getNodeId());
		
		for (String neighbour : this.clusterNodes) {
			try {
				connectionManager.getConnectionManager(neighbour).getClusterAdmimnistration().addNewNode(newNode);
			} catch (RemoteException e) {
				// do nothing, could propagate to that node
			}
		}
		
		this.clusterNodes.add(newNode);
		return ret;
	}

	@Override
	public void disconnectFromGroup(String nodeId) throws RemoteException {
		// TODO: call this where necessary
		if (!isConnected) {
			throw new IllegalArgumentException("El nodo no estaba conectado.");
		}

		System.out.println("disconnecting from group: " + nodeId);

		clusterNodes.remove(nodeId);

		connectionManager.getGroupCommunication().broadcast(
				Messages.newDisconnectMessage(Payloads
						.newDisconnectPayload(nodeId)));

	}

	public Set<String> getClusterNodes() {
		return clusterNodes;
	}

	public void onNodeDisconnected(String disconnectedNode) {
		clusterNodes.remove(disconnectedNode);
	}

}
