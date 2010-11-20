package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

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
			Registry registry = LocateRegistry
					.createRegistry(getClusterPort());
			UnicastRemoteObject.exportObject(this, 0);
			registry.bind(ReferenceName.CONNECTION_MANAGER_NAME, this);
			
			// start message listener
			this.getGroupCommunication();
			
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			e.printStackTrace();
			throw new RemoteException(
					"Connection manager not bound in that node.");
		} catch (AccessException e) {
			// TODO Auto-generated catch block, analize case
			e.printStackTrace();
			throw new RemoteException("REASON UNKNOWN COMPLETE ME!!");
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
		return Registry.REGISTRY_PORT+193;
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
