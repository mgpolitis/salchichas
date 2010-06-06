package domain.services;

import java.util.List;

import pdclogs.LogsClient;
import tgp.TGPClient;
import tmp.TMPServer;
import wdp.WDPServer;

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
	
	public WDPServer getWdpServer();
	
	public void setResource(String resource);
	
	public String getResource();
	
	public void processLogs();
	
	public void fetchResource(String resource, String hostname, int port);
	
	void setParamsToProcess(List<String> countries,	List<String> userAgents, String datesParam);
}
