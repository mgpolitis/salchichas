package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;

import ar.edu.itba.pod.legajo49244.communication.payload.NodeAgentLoadRequestPayloadWalter;
import ar.edu.itba.pod.legajo49244.main.Node;
import ar.edu.itba.pod.legajo49244.message.Messages;
import ar.edu.itba.pod.legajo49244.simulation.DistributedSimulationManager;
import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;
import ar.edu.itba.pod.simul.simulation.Agent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.internal.Lists;
import com.google.inject.internal.Maps;

public class SimulationCommunicationRemote implements SimulationCommunication {

	private static final SimulationCommunicationRemote INSTANCE = new SimulationCommunicationRemote();

	private AtomicBoolean isCoordinator = new AtomicBoolean(false);
	private String coordinatorId = null;
	private boolean isForced = false;

	private SortedSet<NodeAgentLoad> sortedLoadPerNode;
	private Map<String, NodeAgentLoad> mapLoadPerNode;

	private SimulationCommunicationRemote() {
		System.out.println("Creating SimulationCommunication");
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			Node.exportError(getClass());
		}
	}

	public static SimulationCommunicationRemote get() {
		return INSTANCE;
	}

	@Override
	public NodeAgentLoad getMinimumNodeKnownLoad() throws RemoteException {
		if (!isCoordinator.get()) {
			return null;
		}
		NodeAgentLoad lowestLoad = sortedLoadPerNode.first();
		return lowestLoad;
	}

	@Override
	public Collection<AgentDescriptor> migrateAgents(int numberOfAgents)
			throws RemoteException {
		List<AgentDescriptor> migrationBus = Lists.newArrayList();

		for (Agent a : DistributedSimulationManager.get().getAgents()) {
			migrationBus.add(a.getAgentDescriptor());

			// shouldnt we get descriptor AFTER removing?
			DistributedSimulationManager.get().simulation().removeAgent(a);
			System.out.println("Agent " + a.getName()
					+ " is beeing removed from simulation");

			numberOfAgents--;
			if (numberOfAgents <= 0) {
				break;
			}
		}
		return migrationBus;
	}

	@Override
	public void nodeLoadModified(NodeAgentLoad newLoad) throws RemoteException {
		// esto es opcional, podria ignorarse
		if (isCoordinator.get()) {
			this.updateRecordsFor(newLoad);
		}
	}

	@Override
	public void startAgent(AgentDescriptor descriptor) throws RemoteException {
		// aca ya seguro hay que levantar el agent, alguien
		// se ocupo de controlar que sea lo que se debe.

		DistributedSimulationManager.get().addAgentHere(descriptor.build());

		// le aviso al coordinador que cambio mi carga, por cortesia
		int newLoadAmmount = DistributedSimulationManager.get().inspector()
				.runningAgents();
		NodeAgentLoad newLoad = new NodeAgentLoad(Node.getNodeId(),
				newLoadAmmount);
		try {
			ConnectionManager coordCM = this.getCoordinatorConnectionManager();
			if (coordCM != null) {
				coordCM.getSimulationCommunication().nodeLoadModified(newLoad);
			}
		} catch (RemoteException e) {
			// made my best effort to inform, too bad
		}

	}

	public void forcedBecomeCoordinator() {
		// force coordination
		isForced = true;
		isCoordinator.set(false);

		this.becomeCoordinator();
	}

	public synchronized void becomeCoordinator() {
		if (isCoordinator.get()) {
			throw new IllegalStateException("Node is coodinator already.");
		}

		isCoordinator.set(true);
		coordinatorId = null;

		mapLoadPerNode = Maps.newHashMap();
		sortedLoadPerNode = Sets.newTreeSet(new Comparator<NodeAgentLoad>() {

			@Override
			public int compare(NodeAgentLoad o1, NodeAgentLoad o2) {
				int intCmp = new Integer(o1.getNumberOfAgents())
						.compareTo(new Integer(o2.getNumberOfAgents()));
				if (intCmp == 0) {
					return o1.getNodeId().compareTo(o2.getNodeId());
				}
				return intCmp;
			}
		});

		// add your info
		int myLoad = DistributedSimulationManager.get().inspector()
				.runningAgents();
		NodeAgentLoad myAgentLoad = new NodeAgentLoad(Node.getNodeId(), myLoad);
		this.updateRecordsFor(myAgentLoad);

		// get info from all other nodes
		try {
			ClusterCommunicationRemote
					.get()
					.broadcast(
							Messages
									.newNodeAgentLoadRequestMessage(new NodeAgentLoadRequestPayloadWalter()));
		} catch (RemoteException e) {
			// could not broadcast, will have to balance with parcial data
		}
		// deploy waiter thread to make callback after a while
		new Thread(new WaiterRunnable()).start();

	}

	public synchronized void leaveCoordination(String newCoordinator) {
		isCoordinator.set(false);
		mapLoadPerNode = null;
		sortedLoadPerNode = null;
		coordinatorId = newCoordinator;
	}

	public ConnectionManager getCoordinatorConnectionManager() {
		if (isCoordinator.get()) {
			return ConnectionManagerRemote.get();
		}
		if (coordinatorId == null) {
			return null;
		}
		ConnectionManager coordManager = null;
		try {
			coordManager = ConnectionManagerRemote.get().getConnectionManager(
					coordinatorId);
		} catch (RemoteException e) {
			// coordinator was unreachable, will return null
		}
		return coordManager;
	}

	public void onNodeDisconnected(String disconnectedNode) {
		if (Node.getNodeId().equals(disconnectedNode)) {
			try {
				if (ClusterAdministrationRemote.get().isConnectedToGroup()) {
					System.out
							.println("Me dijeron que yo me desconecte y estoy conectado!");
				}
			} catch (RemoteException e) {
				// do nothing, will never fail
			}
		}
		if (isCoordinator.get()) {
			this.removeRecordsFor(disconnectedNode);
		}
	}

	private void removeRecordsFor(String nodeId) {
		Preconditions.checkState(isCoordinator.get());
		NodeAgentLoad disconnectedAgentLoad = mapLoadPerNode.get(nodeId);
		if (disconnectedAgentLoad == null) {
			System.out.println("I was ordered to delete records of a node,"
					+ "but I didn't know of it's existance= " + nodeId);

		} else {
			sortedLoadPerNode.remove(disconnectedAgentLoad);
		}
		mapLoadPerNode.remove(nodeId);

	}

	private void updateRecordsFor(NodeAgentLoad newLoad) {
		Preconditions.checkState(isCoordinator.get());
		Preconditions
				.checkState(mapLoadPerNode.keySet().size() == sortedLoadPerNode
						.size());

		String node = newLoad.getNodeId();
		NodeAgentLoad oldLoad = mapLoadPerNode.get(node);
		if (oldLoad != null) {
			sortedLoadPerNode.remove(oldLoad);
			sortedLoadPerNode.add(newLoad);
		} else {
			sortedLoadPerNode.add(newLoad);
		}
		mapLoadPerNode.put(node, newLoad);

	}

	public void onNodeAgentsLoadInfoArrived(String informerNode, int load) {
		if (isCoordinator.get()) {
			NodeAgentLoad newLoad = new NodeAgentLoad(informerNode, load);
			this.updateRecordsFor(newLoad);
		} else {
			System.out
					.println("node agent load info arrived, but i-m not coordinator AFAIK");
		}
	}

	private void printNodeLoad(String informerNode, int load) {
		System.out.println("\t<Node load info>: " + informerNode + ": " + load);
	}

	public void onWaitTimeForBalancingResponsesExpired() {
		if (!isCoordinator.get()) {
			System.out.println("I'm no longer coordinator, I wont balance");
			return;
		}

		if (isForced) {
			for (NodeAgentLoad al : this.sortedLoadPerNode) {
				this.printNodeLoad(al.getNodeId(), al.getNumberOfAgents());
			}
		}
		System.out.println("Commencing load balancing, doc.");
		int delta = 1000;
		// float mean = -1;
		while (delta >= 2) {

			float sum = 0;
			for (NodeAgentLoad nal : this.sortedLoadPerNode) {
				sum += nal.getNumberOfAgents();
			}
			// mean = sum / this.sortedLoadPerNode.size();

			NodeAgentLoad minLoad = this.sortedLoadPerNode.first();
			NodeAgentLoad maxLoad = this.sortedLoadPerNode.last();
			// System.out.println("MAX = " + maxLoad.getNumberOfAgents());
			// System.out.println("MIN = " + minLoad.getNumberOfAgents());

			delta = maxLoad.getNumberOfAgents() - minLoad.getNumberOfAgents();
			if (delta >= 2) {
				// commence migration process
				String saturatedNode = maxLoad.getNodeId();
				String freeNode = minLoad.getNodeId();
				System.out.println("Migrating " + (delta / 2) + " agents from "
						+ saturatedNode + " to " + freeNode);
				int numberOfAgentsToMigrate = delta / 2;

				try {
					Collection<AgentDescriptor> migrationBus = ConnectionManagerRemote
							.get().getConnectionManager(saturatedNode)
							.getSimulationCommunication().migrateAgents(
									numberOfAgentsToMigrate);
					for (AgentDescriptor immigrant : migrationBus) {
						ConnectionManagerRemote.get().getConnectionManager(
								freeNode).getSimulationCommunication()
								.startAgent(immigrant);
					}
					this.updateRecordsFor(new NodeAgentLoad(saturatedNode,
							maxLoad.getNumberOfAgents()
									- numberOfAgentsToMigrate));
					this.updateRecordsFor(new NodeAgentLoad(freeNode, minLoad
							.getNumberOfAgents()
							+ numberOfAgentsToMigrate));
				} catch (RemoteException e) {
					// TODO stacktrace
					e.printStackTrace();
					System.out
							.println("Error while migrating agents. HECATOMB !!!!!!!!!!");
				}
			} else {
				System.out.println("Nothing to do, this is balanced, yeah!");
			}
		}
		if (isForced) {
			for (NodeAgentLoad al : this.sortedLoadPerNode) {
				this.printNodeLoad(al.getNodeId(), al.getNumberOfAgents());
			}
			isForced = false;
		}

	}

	private class WaiterRunnable implements Runnable {

		private final static long WAIT_TIME = 3000;

		@Override
		public void run() {

			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				// do nothing, stop waiting replies
			}

			SimulationCommunicationRemote.this
					.onWaitTimeForBalancingResponsesExpired();
		}
	}
}
