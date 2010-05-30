package domain.services;

import pdclogs.LogsClient;
import tgp.TGPClient;

public interface WorkerService {

	
	public void badRequest();
	
	public void logError(String error);
	
	public void saveLogs(String logs);
	
	public void setGroup(int group);
	
	public String getWorkerHost();
	
	public int getWorkerPort();
	
	public TGPClient getTgpClient();
	
	public LogsClient getLogsClient();
	
}
