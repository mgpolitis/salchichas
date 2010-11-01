package ar.edu.itba.pod.legajo49244.communication;

import java.io.Serializable;
import java.rmi.RemoteException;

import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;

public class ThreePhaseCommitRemote implements ThreePhaseCommit, Serializable {

	
	private static final ThreePhaseCommit INSTANCE = new ThreePhaseCommitRemote();

	public static ThreePhaseCommit getInstance() {
		return INSTANCE;
	}
	
	private ThreePhaseCommitRemote() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void abort() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canCommit(String coordinatorId, long timeout)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doCommit(String coordinatorId) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTimeout() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void preCommit(String coordinatorId) throws RemoteException {
		// TODO Auto-generated method stub

	}

}
