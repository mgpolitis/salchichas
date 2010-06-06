package domain.services;

import pdclogs.LogsClient;
import tgp.TGPServer;
import tmp.TMPServer;
import marshall.model.EndPoint;

public interface DirectorService extends WDPServerMessageHandler {

	public TMPServer getTMPServer();
	
	public TGPServer getTGPServer();
	
	public LogsClient getLogsClient();
	
	public void addWorker(EndPoint workerEndPoint);

	public void startWorkingSession(EndPoint myEndPoint);
	
}
