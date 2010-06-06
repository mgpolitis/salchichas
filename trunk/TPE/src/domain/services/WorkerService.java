package domain.services;

import marshall.base.BaseServer;
import pdclogs.LogsClient;
import tgp.TGPClient;
import tmp.TMPServer;

public interface WorkerService {

	
	public void badRequest();
	
	public void logError(String error);
	
	public void saveLogs(String logs);
	
	public void setGroup(int group);
	
	public String getWorkerHost();
	
	public int getWorkerPort();
	
	public TGPClient getTgpClient();
	
	public LogsClient getLogsClient();

	public TMPServer getTmpServer();
	
}
