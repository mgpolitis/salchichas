package ar.edu.itba.pod.legajo49244.simulation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import ar.edu.itba.pod.legajo49244.communication.ClusterAdministrationRemote;
import ar.edu.itba.pod.legajo49244.communication.ClusterCommunicationRemote;
import ar.edu.itba.pod.legajo49244.communication.ConnectionManagerRemote;
import ar.edu.itba.pod.legajo49244.communication.SimulationCommunicationRemote;
import ar.edu.itba.pod.legajo49244.communication.TransactionableRemote;
import ar.edu.itba.pod.legajo49244.communication.payload.Payloads;
import ar.edu.itba.pod.legajo49244.dispatcher.DispatcherListener;
import ar.edu.itba.pod.legajo49244.main.Node;
import ar.edu.itba.pod.legajo49244.message.Messages;
import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.Transactionable;
import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataPayload;
import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;

public class DistributedSimulationManager implements SimulationManager,
		DispatcherListener {

	private static final int MAX_ROUNDROBIN_TIMES = 100;
	private static final long TRANSACTION_TIMEOUT = 1000;
	private static DistributedSimulationManager INSTANCE = new DistributedSimulationManager();
	private boolean isStarted = false;
	
	private DistributedSimulationManager() {
	}

	public static DistributedSimulationManager get() {
		return INSTANCE;
	}

	private DistributedSimulation simulation;

	public void setTimeMapper(TimeMapper timeMapper) {
		simulation = new DistributedSimulation(timeMapper);
	}

	@Override
	public void start() {
		simulation.start();
		SimulationCommunicationRemote.get().becomeCoordinator();
		isStarted = true;
	}

	@Override
	public void shutdown() {
		Deque<AgentDescriptor> ads = new LinkedList<AgentDescriptor>();
		for (Agent agent : this.simulation().getAgents()) {
			AgentDescriptor ad = agent.getAgentDescriptor();
			ads.add(ad);
			this.simulation().removeAgent(agent);
		}
		System.out.println("agents removed from local simulation");

		if (ClusterAdministrationRemote.get().getClusterNodes().size() == 0) {
			System.out
					.println("I knew no other nodes in cluster, so I cant migrate agents.");
			return;
		}
		int time = 0;
		while (ads.size() != 0) {
			time++;
			for (String node : ClusterAdministrationRemote.get()
					.getClusterNodes()) {
				if (ads.isEmpty()) {
					break;
				}
				AgentDescriptor ad = ads.pop();
				try {
					ConnectionManagerRemote.get().getConnectionManager(node)
							.getSimulationCommunication().startAgent(ad);
					System.out.println("Agent migrated, " + ads.size()
							+ " remaining...");
				} catch (RemoteException e) {
					ads.push(ad);
				}
			}
			if (time > MAX_ROUNDROBIN_TIMES) {
				System.out.println("Timeout!");
				return;
			}
		}

		try {
			ConnectionManagerRemote.get().getClusterAdmimnistration()
					.disconnectFromGroup(Node.getNodeId());
		} catch (RemoteException e) {
			System.out.println("Cant tell others to disconnect");
			e.printStackTrace();
		}

		simulation().shutdown();
		isStarted = false;
	}

	@Override
	public void addAgent(Agent agent) {
		// check where in the world to insert, and do it!

		ConnectionManager ccm = SimulationCommunicationRemote.get()
				.getCoordinatorConnectionManager();
		if (ccm == null) {
			// coordinator is not known or down
			this.noCoordinatorKnownAddAgent(agent);
		} else {
			try {
				// coordinator is known
				NodeAgentLoad nodeAgentLoad = ccm.getSimulationCommunication()
						.getMinimumNodeKnownLoad();
				if (nodeAgentLoad == null) {
					// coordinator was UP, but he is no longer coordinator
					this.noCoordinatorKnownAddAgent(agent);
				} else {
					// coordinator is UP and informed of lower load node
					System.out.println("I think  node "
							+ nodeAgentLoad.getNodeId() + " has "
							+ nodeAgentLoad.getNumberOfAgents()
							+ " and is the node with lowest node load.");
					String minNode = nodeAgentLoad.getNodeId();
					AgentDescriptor descriptor = agent.getAgentDescriptor();
					ConnectionManagerRemote.get().getConnectionManager(minNode)
							.getSimulationCommunication()
							.startAgent(descriptor);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				this.noCoordinatorKnownAddAgent(agent);
			}
		}
	}

	private void noCoordinatorKnownAddAgent(Agent agent) {
		// no coordinator is known, become TEH COORDINATORRRRR
		this.addAgentHere(agent);
		SimulationCommunicationRemote.get().becomeCoordinator();
	}

	public void addAgentHere(Agent agent) {
		simulation.addAgent(agent);
		if (isStarted) {
			agent.start();
		}
	}

	@Override
	public Collection<Agent> getAgents() {
		return simulation.getAgents();
	}

	@Override
	public SimulationInspector inspector() {
		return simulation();
	}

	@Override
	public <T> void register(Class<T> type, T instance) {
		simulation.register(type, instance);
	}

	@Override
	public void removeAgent(Agent agent) {
		// no se llamara externamente
		this.simulation.removeAgent(agent);

	}

	@Override
	public DistributedSimulation simulation() {
		return simulation;
	}

	public void forceCoordinate() {
		SimulationCommunicationRemote.get().becomeCoordinator();
	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	@Override
	public boolean onDisconnect(Message message) {
		String disconnectedNode = ((DisconnectPayload) message.getPayload())
				.getDisconnectedNodeId();
		ClusterAdministrationRemote.get().onNodeDisconnected(disconnectedNode);
		SimulationCommunicationRemote.get()
				.onNodeDisconnected(disconnectedNode);

		return true;
	}

	@Override
	public boolean onNodeAgentsLoad(Message message) {
		int load = ((NodeAgentLoadPayload) message.getPayload()).getLoad();
		String informerNode = message.getNodeId();
		SimulationCommunicationRemote.get().onNodeAgentsLoadInfoArrived(
				informerNode, load);
		return true;
	}

	@Override
	public boolean onNodeAgentsLoadRequest(Message message) {
		// sender node is coordinator, tell him your load
		String coordId = message.getNodeId();
		SimulationCommunicationRemote.get().leaveCoordination(coordId);

		int load = inspector().runningAgents();
		try {
			ClusterCommunicationRemote.get().send(
					Messages.newNodeAgentLoadResponseMessage(Payloads
							.newNodeAgentLoadPayload(load)), coordId);
		} catch (RemoteException e) {
			// too bad, could not inform my load...
		}
		return true;
	}

	public void sendGetClusterMarketData() {
		System.out.println("Requesting CLUSTER MARKET DATA");
		try {
			ClusterCommunicationRemote.get().broadcast(
					Messages.newNodeMarketDataRequestMessage(Payloads
							.newNodeMarketDataRequestPayload()));
		} catch (RemoteException e) {
			// could not broadcast
			e.printStackTrace();
		}
		System.out.println("\t ~~~ "+Node.getNodeId()+": ");
		System.out.println("\t t/sec = "+DistributedMarketManager.get().market().marketData().getHistory().getTransactionsPerSecond());
	}

	@Override
	public boolean onNodeMarketData(Message message) {
		NodeMarketDataPayload payload = (NodeMarketDataPayload) message.getPayload();
		System.out.println("\t ~~~ "+message.getNodeId()+": ");
		System.out.println("\t t/sec = "+payload.getMarketData().getHistory().getTransactionsPerSecond());
		return true;
	}

	@Override
	public boolean onNodeMarketDataRequest(Message message) {
		NodeMarketDataPayload payload = Payloads
				.newNodeMarketDataPayload(DistributedMarketManager.get()
						.market().marketData());
		try {
			ClusterCommunicationRemote.get().send(
					Messages.newNodeMarketDataResponseMessage(payload),
					message.getNodeId());
		} catch (Exception e) {
			// could not send market data
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean onResourceRequest(Message message) {
		// Nodo A no tiene un recurso, manda broadcast
		ResourceRequestPayload payload = (ResourceRequestPayload) message
				.getPayload();

		// Nodo B que recibe el mensaje y tiene el recurso,
		boolean didPrepare = DistributedMarketManager.get().market()
				.prepareToExportifYouHave(payload.getResource(),
						payload.getAmountRequested());

		if (didPrepare) {
			Transactionable otherTransactionable;
			Transactionable myTransactionable;
			try {
				myTransactionable = TransactionableRemote.get();
				otherTransactionable = ConnectionManagerRemote.get()
						.getConnectionManager(message.getNodeId())
						.getNodeCommunication();

				try {
					// invoca al beginTransaction con el nodo A como parametro
					myTransactionable.beginTransaction(message.getNodeId(),
							TRANSACTION_TIMEOUT);
					// Si no falla, el nodo B, invoca el exchange tanto en el
					// mismo como en el nodo A.
					otherTransactionable.exchange(payload.getResource(),
							payload.getAmountRequested(), Node.getNodeId(),
							message.getNodeId());
					myTransactionable.exchange(payload.getResource(), payload
							.getAmountRequested(), Node.getNodeId(), message
							.getNodeId());
					// Luego, el nodo B invoca el endTransaction.
					myTransactionable.endTransaction();
				} catch (IllegalStateException e) {
					// some other node started transaction first
					//DistributedMarketManager.get().market().importResources(payload.getResource(), payload.getAmountRequested());
				}

			} catch (RemoteException e) {
				// could not establish transaction
			}
		}
		return true;
	}
}
