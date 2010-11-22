package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.google.common.base.Preconditions;

import ar.edu.itba.pod.legajo49244.main.Node;
import ar.edu.itba.pod.legajo49244.simulation.DistributedMarket;
import ar.edu.itba.pod.legajo49244.simulation.DistributedMarketManager;
import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;

public class ThreePhaseCommitRemote implements ThreePhaseCommit {

	public enum State {
		IDLE, CAN_COMMIT_CALLED, PRE_COMMIT_CALLED, DO_COMMIT_CALLED
	}

	private String coordId = null;
	private State state = State.IDLE;

	private static final ThreePhaseCommit INSTANCE = new ThreePhaseCommitRemote();

	public static ThreePhaseCommit get() {
		return INSTANCE;
	}

	private ThreePhaseCommitRemote() {
		if (Node.isVerbose())
			System.out.println("creating ThreePhaseCommit");
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			Node.exportError(this.getClass());
		}
	}

	/**
	 * Called by the coordinator to gather votes for doing a commit. The cohorts
	 * can return true or false, whether they are ready or not. If false is
	 * returned, the coordinator aborts the commit. If true is returned, the
	 * coordinator calls preCommit method in all the cohorts. The cohorts will
	 * wait for the timeout set by parameter before canceling the process. Once
	 * the cohort is prepared to commit, only the same coordinator can invoke
	 * preCommit and doCommit until the whole process is done.
	 * 
	 * @param coordinatorId
	 *            The coordinator identification
	 */
	public synchronized boolean canCommit(String coordinatorId, long timeout)
			throws RemoteException {
		if (Node.isVerbose())
			System.out.println("****canCommit Called");
		if (coordId != null) {
			// interface doesnt say I should throw exception, :(, god help us
		}
		coordId = coordinatorId;
		// El timeOut lo necesitas para controlar si la transacción se quedo
		// colgada en el medio.
		new Thread(new WaiterRunnable(timeout)).start();

		this.state = State.CAN_COMMIT_CALLED;
		return true;
	}

	/**
	 * If the cohort is in prepared state, a commit is done. Only the same
	 * coordinator that invoked canCommit can invoke this method. Otherwise, an
	 * IllegalArgumentException is thrown. If it is invoked before canCommit
	 * method, and IllegalStateException is thrown. At this stage, the cohort
	 * does make the commit.
	 * 
	 * @param coordinatorId
	 *            The coordinator identification
	 */
	public synchronized void preCommit(String coordinatorId) throws RemoteException {
		if (Node.isVerbose())
			System.out.println("****preCommit Called");
		if (!coordinatorId.equals(coordinatorId)) {
			throw new IllegalArgumentException(
					"Coordinator for this 3PC can only be " + coordinatorId);
		}
		if (!this.state.equals(State.CAN_COMMIT_CALLED)) {
			throw new IllegalStateException("canCommit must be called first!");
		}

		this.state = State.PRE_COMMIT_CALLED;
		return;
	}

	/**
	 * Method called by the coordinator to change to commit state after
	 * receiving OK from all. Only the same coordinator that invoked preCommit
	 * can invoke this method. Otherwise, an IllegalArgumentException is thrown.
	 * If it is invoked before preCommit method, and IllegalStateException is
	 * thrown. After this method, any coordinator can start the whole process
	 * again.
	 * 
	 * @param coordinatorId
	 *            The coordinator identification
	 */
	public synchronized void doCommit(String coordinatorId) throws RemoteException {
		if (Node.isVerbose())
			System.out.println("****doCommit Called");
		if (!coordinatorId.equals(coordinatorId)) {
			throw new IllegalArgumentException(
					"Coordinator for this 3PC can only be " + coordinatorId);
		}
		if (!this.state.equals(State.PRE_COMMIT_CALLED)) {
			throw new IllegalStateException("canCommit must be called first!");
		}

		this.ultraDoCommit();

		return;
	}

	private synchronized void ultraDoCommit() {
		if (Node.isVerbose())
			System.out.println("**** ultraDoCommit called");
		ResourceTransferMessagePayload payload = null;
		try {
			payload = (ResourceTransferMessagePayload) TransactionableRemote
					.get().getPayload();
		} catch (RemoteException e) {
			// will never fail, local call
		}
		DistributedMarket market = DistributedMarketManager.get().market();
		if (payload.getSource().equals(Node.getNodeId())) {
			// i have to give resources
			market.exportResources(payload.getResource(), payload.getAmount());
		} else {
			// add the resources to my market
			market.importResources(payload.getResource(), payload.getAmount());
		}
		this.state = State.DO_COMMIT_CALLED;
	}

	/**
	 * Method called when an abort message is received during the commit. All
	 * the changes done must be reverted. If it is invoked before canCommit
	 * method, and IllegalStateException is thrown.
	 * 
	 * @throws RemoteException
	 */
	public synchronized void abort() throws RemoteException {
		if (Node.isVerbose())
			System.out.println("****abort Called");
		Preconditions.checkState(!this.state.equals(State.IDLE),
				"canCommit must be called first!");


		switch (this.state) {
		case CAN_COMMIT_CALLED:
			this.coordId = null;
			this.state = State.IDLE;
			break;
		case PRE_COMMIT_CALLED:
			this.coordId = null;
			this.state = State.IDLE;
			break;
		case DO_COMMIT_CALLED:
			ResourceTransferMessagePayload payload = null;
			try {
				payload = (ResourceTransferMessagePayload) TransactionableRemote
						.get().getPayload();
			} catch (RemoteException e) {
				// will never fail, local call
			}
			DistributedMarket market = DistributedMarketManager.get().market();
			if (payload.getDestination().equals(Node.getNodeId())) {
				// i had to receive resources, to abort liberate them (export)
				market.exportResources(payload.getResource(), payload
						.getAmount());
			} else {
				// i had to give resources, to abort retrive them! (import)
				market.importResources(payload.getResource(), payload
						.getAmount());
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Method called when the cohort waits more than the timeout set for doing
	 * the commit. All the changes done must be reverted. If it is invoked
	 * before canCommit method, and IllegalStateException is thrown.
	 * 
	 * @throws RemoteException
	 */
	public synchronized void onTimeout() throws RemoteException {
		if (Node.isVerbose()) {
			System.out.println("****onTimeout Called");
		}
		Preconditions.checkState(!this.state.equals(State.IDLE),
				"canCommit must be called first!");

		if (this.state.equals(State.CAN_COMMIT_CALLED)) {
			this.abort();
		}

		if (this.state.equals(State.PRE_COMMIT_CALLED)) {
			this.ultraDoCommit();
		}
		
		if (this.state.equals(State.DO_COMMIT_CALLED)) {
			this.coordId = null;
			this.state = State.IDLE;
		}
		
		TransactionableRemote.get().cleanContext();

	}

	private class WaiterRunnable implements Runnable {

		private long waitTime;

		public WaiterRunnable(long waitTime) {
			this.waitTime = waitTime;
		}

		@Override
		public void run() {

			try {
				Thread.sleep(this.waitTime);
			} catch (InterruptedException e) {
				// do nothing, and will probably abort transaction :(
			}

			try {
				ThreePhaseCommitRemote.this.onTimeout();
			} catch (Exception e) {
				// will never fail
			}
		}
	}

}
