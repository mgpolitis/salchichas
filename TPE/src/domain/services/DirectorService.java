package domain.services;

import marshall.model.EndPoint;
import wdp.WDPServer;

public interface DirectorService {

	
	public void addWorker(EndPoint workerEndPoint);

	public void startWorkingSession(EndPoint myEndPoint);
	
}
