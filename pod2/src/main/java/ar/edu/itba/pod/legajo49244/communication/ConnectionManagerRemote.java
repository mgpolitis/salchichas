package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import ar.edu.itba.pod.simul.communication.ClusterAdministration;
import ar.edu.itba.pod.simul.communication.ClusterCommunication;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.ReferenceName;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;

public class ConnectionManagerRemote implements ConnectionManager {

	private static final ConnectionManager INSTANCE = new ConnectionManagerRemote();

	public static ConnectionManager getInstance() {
		return INSTANCE;
	}

	private ConnectionManagerRemote() {
		System.out.println("Creating ConnectionManager");
		try {
			Registry registry = LocateRegistry
					.createRegistry(Registry.REGISTRY_PORT);
			UnicastRemoteObject.exportObject(this, 0);
			registry.bind(ReferenceName.CONNECTION_MANAGER_NAME, this);
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
		final Registry registry = LocateRegistry.getRegistry(nodeId);
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

			this.getClusterAdmimnistration().disconnectFromGroup(nodeId);

			e.printStackTrace();
			// TODO see if this exception needs to be raised
			throw new RemoteException("Node was found to be down.");
		}
		return ret;
	}

	@Override
	public ClusterAdministration getClusterAdmimnistration()
			throws RemoteException {
		return ClusterAdministrationRemote.getInstance();
	}

	@Override
	public int getClusterPort() throws RemoteException {
		return Registry.REGISTRY_PORT;
	}

	@Override
	public ClusterCommunication getGroupCommunication() throws RemoteException {
		return ClusterCommunicationRemote.getInstance();
	}

	@Override
	public Transactionable getNodeCommunication() throws RemoteException {
		return TransactionableRemote.getInstance();
	}

	@Override
	public SimulationCommunication getSimulationCommunication()
			throws RemoteException {
		return SimulationCommunicationRemote.getInstance();
	}

	@Override
	public ThreePhaseCommit getThreePhaseCommit() throws RemoteException {
		return ThreePhaseCommitRemote.getInstance();
	}

}
