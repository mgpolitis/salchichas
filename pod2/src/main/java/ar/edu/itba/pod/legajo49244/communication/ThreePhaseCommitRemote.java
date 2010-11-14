package ar.edu.itba.pod.legajo49244.communication;



import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;

public class ThreePhaseCommitRemote implements ThreePhaseCommit {

	public enum State {
		IDLE,
		CAN_COMMIT_CALLED,
		PRE_COMMIT_CALLED,
		ABORT // is this necesaary?
	}
	
	
	private String coordId = null;
	private State state = State.IDLE;
	
	private static final ThreePhaseCommit INSTANCE = new ThreePhaseCommitRemote();
	
	public static ThreePhaseCommit get() {
		return INSTANCE;
	}
	
	private ThreePhaseCommitRemote() {
		System.out.println("creating ThreePhaseCommit");
		try {
			UnicastRemoteObject.exportObject(this,0);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Called by the coordinator to gather votes for doing a commit. The cohorts can return true or false, whether they
	 * are ready or not. If false is returned, the coordinator aborts the commit. If true is returned, the coordinator
	 * calls preCommit method in all the cohorts. The cohorts will wait for the timeout set by parameter before
	 * canceling the process. Once the cohort is prepared to commit, only the same coordinator can invoke preCommit and
	 * doCommit until the whole process is done.
	 * 
	 * @param coordinatorId
	 *            The coordinator identification
	 */
	public boolean canCommit(String coordinatorId, long timeout) throws RemoteException {
		if (coordId != null) {
			System.out.println("ALREADY HAD COORDINATOR, ALTO QUILOMBO!!!");
		}
		coordId = coordinatorId;
		
		// TODO: do waiter thread and that stuff
		this.state = State.CAN_COMMIT_CALLED;
		return false;
	}

	/**
	 * If the cohort is in prepared state, a commit is done. Only the same coordinator that invoked canCommit can invoke
	 * this method. Otherwise, an IllegalArgumentException is thrown. If it is invoked before canCommit method, and
	 * IllegalStateException is thrown. At this stage, the cohort does make the commit.
	 * 
	 * @param coordinatorId
	 *            The coordinator identification
	 */
	public void preCommit(String coordinatorId) throws RemoteException {
		if (!coordinatorId.equals(coordinatorId)) {
			throw new IllegalArgumentException("Coordinator for this 3PC can only be "+coordinatorId);
		}
		if (!this.state.equals(State.CAN_COMMIT_CALLED)) {
			throw new IllegalStateException("canCommit must be called first!");
		}
		
		//TODO: do
		
		this.state = State.PRE_COMMIT_CALLED;
		return;
	}

	/**
	 * Method called by the coordinator to change to commit state after receiving OK from all. Only the same coordinator
	 * that invoked preCommit can invoke this method. Otherwise, an IllegalArgumentException is thrown. If it is invoked
	 * before preCommit method, and IllegalStateException is thrown. After this method, any coordinator can start the
	 * whole process again.
	 * 
	 * @param coordinatorId
	 *            The coordinator identification
	 */
	public void doCommit(String coordinatorId) throws RemoteException {
		if (!coordinatorId.equals(coordinatorId)) {
			throw new IllegalArgumentException("Coordinator for this 3PC can only be "+coordinatorId);
		}
		if (!this.state.equals(State.PRE_COMMIT_CALLED)) {
			throw new IllegalStateException("canCommit must be called first!");
		}
		// TODO: do
		
		coordinatorId = null;
		this.state = State.IDLE;
		return;
	}

	/**
	 * Method called when an abort message is received during the commit. All the changes done must be reverted. If it
	 * is invoked before canCommit method, and IllegalStateException is thrown.
	 * 
	 * @throws RemoteException
	 */
	public void abort() throws RemoteException {
		// TODO: ver si este check esta bien
		if (!this.state.equals(State.IDLE)) {
			throw new IllegalStateException("canCommit must be called first!");
		}
		
		//TODO: do
	}

	/**
	 * Method called when the cohort waits more than the timeout set for doing the commit. All the changes done must be
	 * reverted. If it is invoked before canCommit method, and IllegalStateException is thrown.
	 * 
	 * @throws RemoteException
	 */
	public void onTimeout() throws RemoteException {
		// TODO: ver si este check esta bien
		if (!this.state.equals(State.IDLE)) {
			throw new IllegalStateException("canCommit must be called first!");
		}
		
		//TODO: do
	}
	
	
	
}
