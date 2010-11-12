package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;

import ar.edu.itba.pod.legajo49244.Node;
import ar.edu.itba.pod.legajo49244.communication.payload.NodeAgentLoadRequestPayloadWalter;
import ar.edu.itba.pod.legajo49244.message.Messages;
import ar.edu.itba.pod.legajo49244.simulation.DistributedSimulationManager;
import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.internal.Maps;

public class SimulationCommunicationRemote implements SimulationCommunication {

	private static final SimulationCommunicationRemote INSTANCE = new SimulationCommunicationRemote();

	private boolean isCoordinator = false;
	private String coordinatorId = null;

	private SortedSet<NodeAgentLoad> sortedLoadPerNode;
	private Map<String, NodeAgentLoad> mapLoadPerNode;

	private SimulationCommunicationRemote() {
		System.out.println("Creating SimulationCommunication");
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static SimulationCommunicationRemote get() {
		return INSTANCE;
	}

	@Override
	public NodeAgentLoad getMinimumNodeKnownLoad() throws RemoteException {
		if (!isCoordinator) {
			return null;
		}
		System.out.println("I-ve been requested node with min load, lets see:");
		for (NodeAgentLoad nal : sortedLoadPerNode){ 
			System.out.println(nal.getNodeId()+":"+nal.getNumberOfAgents());
		}
		NodeAgentLoad lowestLoad = sortedLoadPerNode.first();
		return lowestLoad;
	}

	@Override
	public Collection<AgentDescriptor> migrateAgents(int numberOfAgents)
			throws RemoteException {
		// TODO get agents from local simulation and release them!
		return null;
	}

	@Override
	public void nodeLoadModified(NodeAgentLoad newLoad) throws RemoteException {

		// TODO: todo esto es opcional, analizar si se deja o se saca
		if (isCoordinator) {
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

	public synchronized void becomeCoordinator() {
		if (isCoordinator) {
			throw new IllegalStateException("Node is coodinator already.");
		}

		isCoordinator = true;
		coordinatorId = null;
		
		sortedLoadPerNode = Sets.newTreeSet(new Comparator<NodeAgentLoad>() {

			@Override
			public int compare(NodeAgentLoad o1, NodeAgentLoad o2) {
				return new Integer(o1.getNumberOfAgents())
						.compareTo(new Integer(o2.getNumberOfAgents()));
			}
		});
		mapLoadPerNode = Maps.newHashMap();

		// add your info
		int myLoad = DistributedSimulationManager.get().inspector()
				.runningAgents();
		NodeAgentLoad myAgentLoad = new NodeAgentLoad(Node.getNodeId(), myLoad);
		this.updateRecordsFor(myAgentLoad);

		// TODO: analyze fully

		// get info from all other nodes
		try {
			ClusterCommunicationRemote
					.get()
					.broadcast(
							Messages
									.newNodeAgentLoadRequestMessage(new NodeAgentLoadRequestPayloadWalter()));
		} catch (RemoteException e) {
			// TODO: formalize this syso or delete it
			System.out.println("SHOULD NEVER REACH HERE");
		}
		// deploy waiter thread to make callback after a while
		new Thread(new WaiterRunnable()).start();

	}

	public synchronized void leaveCoordination(String newCoordinator) {
		isCoordinator = false;
		mapLoadPerNode = null;
		sortedLoadPerNode = null;
		coordinatorId = newCoordinator;
	}

	public ConnectionManager getCoordinatorConnectionManager() {
		if (isCoordinator) {
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
		// TODO: ver los sysos estos, son informales
		if (Node.getNodeId().equals(disconnectedNode)) {
			try {
				if (ClusterAdministrationRemote.get().isConnectedToGroup()) {
					System.out
							.println("Me dijeron que yo me desconecte y estoy conectado! WTF? ");
				}
			} catch (RemoteException e) {
				System.out.println("ESTO NO DEBERIA APARECER NUNCA");
				// do nothing, will never fail
			}
		}
		if (isCoordinator) {
			this.removeRecordsFor(disconnectedNode);
		}
	}

	private void removeRecordsFor(String nodeId) {
		Preconditions.checkState(isCoordinator);
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
		Preconditions.checkState(isCoordinator);
		String node = newLoad.getNodeId();
		NodeAgentLoad oldLoad = mapLoadPerNode.get(node);
		if (oldLoad != null) {
			sortedLoadPerNode.remove(oldLoad);
			sortedLoadPerNode.add(newLoad);
		} else {
			sortedLoadPerNode.add(newLoad);
		}
		mapLoadPerNode.put(node, newLoad);
		System.out.println("this should be always true: "
				+ (mapLoadPerNode.keySet().size() == sortedLoadPerNode.size()));
	}

	public void onNodeAgentsLoadInfoArrived(String informerNode, int load) {
		if (isCoordinator) {
			NodeAgentLoad newLoad = new NodeAgentLoad(informerNode, load);
			this.updateRecordsFor(newLoad);
		} else {
			System.out
					.println("node agent load info arrived, but i-m not coordinator AFAIK");
		}
	}

	public void onWaitTimeForBalancingResponsesExpired() {
		// TODO balance
		System.out.println("Should be balancing now... l'doit later, dude...");
	}

	private class WaiterRunnable implements Runnable {

		private final static long WAIT_TIME = 2000;

		@Override
		public void run() {

			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			SimulationCommunicationRemote.this
					.onWaitTimeForBalancingResponsesExpired();
		}
	}
}
