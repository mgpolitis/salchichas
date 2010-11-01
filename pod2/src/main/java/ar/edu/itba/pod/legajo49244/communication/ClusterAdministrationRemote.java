package ar.edu.itba.pod.legajo49244.communication;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import ar.edu.itba.pod.legajo49244.Node;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ConnectionManager;

public class ClusterAdministrationRemote implements ClusterAdministration,
		Serializable {

	private static final ClusterAdministration INSTANCE = new ClusterAdministrationRemote();
	private static final String DEFAULT_CLUSTER_NAME = "eva";
	
	private Set<String> clusterNodes = new HashSet<String>();

	private String clusterName = null;
	private boolean isConnected = false;
	private ConnectionManager connectionManager = ConnectionManagerRemote.getInstance();

	private ClusterAdministrationRemote() {
		// TODO Auto-generated constructor stub
	}

	public static ClusterAdministration getInstance() {
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
	public void connectToGroup(String initialNode) throws RemoteException {
		if (initialNode.equals(Node.NODE_ID)) {
			throw new IllegalArgumentException(
					"Cannot connect to cluster through yourself, node!");
		}
		if (this.isConnectedToGroup()) {
			throw new IllegalStateException(
					"Cannot reconnect, node is already connected to a cluster");
		}
		
		ConnectionManager initialCM = connectionManager.getConnectionManager(initialNode);
		Iterable<String> nodes = initialCM.getClusterAdmimnistration().addNewNode(Node.NODE_ID);
		for (String node : nodes) {
			clusterNodes.add(node);
		}
		isConnected = true;

	}

	@Override
	public Iterable<String> addNewNode(String newNode) throws RemoteException {
		// TODO: hacelo
		return null;
	}

	@Override
	public void disconnectFromGroup(String nodeId) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getGroupId() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
