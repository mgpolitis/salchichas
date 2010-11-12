package ar.edu.itba.pod.legajo49244;

import java.rmi.RemoteException;

import ar.edu.itba.pod.legajo49244.communication.ConnectionManagerRemote;
import ar.edu.itba.pod.legajo49244.simulation.DistributedMarketManager;
import ar.edu.itba.pod.legajo49244.simulation.DistributedSimulationManager;
import ar.edu.itba.pod.simul.ObjectFactory;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;

import com.google.common.base.Preconditions;

public class MegaEpicFactory implements ObjectFactory {

	private ConnectionManager connectionManager;
	private MarketManager marketManager;
	private DistributedSimulationManager simulationManager;

	@Override
	public ConnectionManager createConnectionManager(String localIp) {
		Preconditions.checkNotNull(localIp);
		return getConnectionManager(localIp, null);
	}

	@Override
	public ConnectionManager createConnectionManager(String localIp,
			String groupIp) {
		Preconditions.checkNotNull(localIp);
		Preconditions.checkNotNull(groupIp);
		return getConnectionManager(localIp, groupIp);
	}

	private ConnectionManager getConnectionManager(String localIP,
			String entryPoint) {
		if (connectionManager != null) {
			throw new IllegalStateException(
					"Cannot create connectionManager twice.");
		}
		System.out.println("User set my nodeId to " + localIP);
		Node.setUserNodeId(localIP);

		if (entryPoint != null) {
			System.out.println("User set my entry point to cluster as "
					+ entryPoint);
			Node.setUserEntryPoint(entryPoint);
		}

		connectionManager = ConnectionManagerRemote.get();
		System.out.println("My node id is " + Node.getNodeId());

		try {
			if (Node.getEntryPoint() == null) {
				connectionManager.getClusterAdmimnistration().createGroup();
			} else {
				connectionManager.getClusterAdmimnistration().connectToGroup(
						Node.getEntryPoint());
			}

		} catch (RemoteException e) {
			System.out
					.println("There was a problem joining the cluster, now exiting");
			System.out.println("Reason: ");
			System.out.println("\t+ " + e.getMessage());
			System.exit(0);
		}

		// create Market Manager and store
		marketManager = new DistributedMarketManager();

		// create SimulationManager and store
		simulationManager = DistributedSimulationManager.get();

		return connectionManager;
	}

	@Override
	public MarketManager getMarketManager(ConnectionManager mgr) {
		Preconditions.checkNotNull(mgr);
		if (marketManager == null) {
			throw new IllegalStateException(
					"Must create connection manager with this Factory first.");
		}
		return marketManager;
	}

	@Override
	public SimulationManager getSimulationManager(ConnectionManager mgr,
			TimeMapper timeMapper) {
		Preconditions.checkNotNull(mgr);
		if (simulationManager == null) {
			throw new IllegalStateException(
					"Must create connection manager with this Factory first.");
		}
		simulationManager.setTimeMapper(timeMapper);
		return simulationManager;
	}

}
