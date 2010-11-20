package ar.edu.itba.pod.legajo49244.communication;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;

import ar.edu.itba.pod.legajo49244.main.MySocketFactory;
import ar.edu.itba.pod.legajo49244.main.Node;
import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.ReferenceName;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;

public class ConnectionManagerRemote implements ConnectionManager {

	private static final ConnectionManager INSTANCE = new ConnectionManagerRemote();

	public static ConnectionManager get() {
		return INSTANCE;
	}

	private ConnectionManagerRemote() {
		System.out.println("Creating ConnectionManager");
		try {
			int port = getClusterPort();
			RMISocketFactory csf = new MySocketFactory(InetAddress.getByName(Node.getNodeId()));
			Registry registry = LocateRegistry.createRegistry(port, csf, csf);
			UnicastRemoteObject.exportObject(this, 0);
			registry.bind(ReferenceName.CONNECTION_MANAGER_NAME, this);
			
			// start message listener
			this.getGroupCommunication();
			
		} catch (AlreadyBoundException e) {
			System.out.println("Port,address combination already bound");
			System.out.println(e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Remote exception while creating RMI registry");
			System.out.println(e.getMessage());
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
			System.out.println(e.getMessage());
		} 
	}

	@Override
	public ConnectionManager getConnectionManager(String nodeId)
			throws RemoteException {
		if (nodeId.equals(Node.getNodeId())) {
			return this;
		}
		final Registry registry = LocateRegistry.getRegistry(nodeId,getClusterPort());
		ConnectionManager ret;
		try {
			ret = (ConnectionManager) registry
					.lookup(ReferenceName.CONNECTION_MANAGER_NAME);
		} catch (NotBoundException e) {
			throw new RemoteException(
					"Connection manager not bound in that node.");
		} catch (AccessException e) {
			throw new RemoteException("Access Exception");
		} catch (RemoteException e) {

			String message = "Node "+nodeId+" was found to be down.";
			System.out.println(message);
			if (this.getClusterAdmimnistration().isConnectedToGroup()) {
				// if connected to cluster notify others of node down
				this.getClusterAdmimnistration().disconnectFromGroup(nodeId);
			}

			throw new RemoteException(message);
		}
		return ret;
	}

	@Override
	public ClusterAdministration getClusterAdmimnistration()
			throws RemoteException {
		return ClusterAdministrationRemote.get();
	}

	@Override
	public int getClusterPort() throws RemoteException {
		return Registry.REGISTRY_PORT+141;
	}

	@Override
	public ClusterCommunication getGroupCommunication() throws RemoteException {
		return ClusterCommunicationRemote.get();
	}
	
	@Override
	public Transactionable getNodeCommunication() throws RemoteException {
		return TransactionableRemote.get();
	}

	@Override
	public SimulationCommunication getSimulationCommunication()
			throws RemoteException {
		return SimulationCommunicationRemote.get();
	}

	@Override
	public ThreePhaseCommit getThreePhaseCommit() throws RemoteException {
		return ThreePhaseCommitRemote.get();
	}

}
