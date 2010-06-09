package domain.services;

import marshall.model.EndPoint;
import pdclogs.LogsClient;
import tgp.TGPClient;
import tmp.TMPServer;
import wdp.WDPServer;

public interface WorkerService extends ProtocolsMessageHandler{

	
	public void badRequest();
	
	public void logError(String error);
	
	public void setGroup(int group);
	
	public String getWorkerHost();
	
	public int getWorkerPort();
	
	public TGPClient getTgpClient();
	
	public LogsClient getLogsClient();

	public TMPServer getTmpServer();
	
	public WDPServer getWdpServer();
	
	public void setResource(String resource);

	public EndPoint getDirector();

	public int getJobsDone();

	public int getLinesProcessed();
	
}
