package ar.edu.itba.pod.legajo49244.communication;



import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ar.edu.itba.pod.legajo49244.Node;
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
			Node.exportError(this.getClass());
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
			// interface doesnt say I should throw exception, :(, god help us
			System.out.println("ALREADY HAD COORDINATOR, ALTO QUILOMBO!!!");
		}
		coordId = coordinatorId;
		// El timeOut lo necesitas para controlar si la transacción se quedo colgada en el medio.
		new Thread(new WaiterRunnable(timeout)).start();
		
		this.state = State.CAN_COMMIT_CALLED;
		return true;
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
		
		//TODO: do changes! wiii
//		La implementación de este método, en el nodo A, pide el Payload,
//		genera un mensaje con el recurso a aumentar. El nodo B hace lo mismo pero para disminuir.
		
		//ResourceTransferMessagePayload payload = (ResourceTransferMessagePayload) TransactionableRemote.get().getPayload();
		// TODO: agarrar el market y hacer los cambios
		
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
		// TODO: do, tengo que hacer algo???? me parece q nada
		
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
		
		//TODO: do, depnde del state que hago?
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
			} catch (RemoteException e) {
				System.out.println("remote exception on waiter runnable of 3PC");
				e.printStackTrace();
				// will never fail
			}
		}
	}
	
	
	
}
