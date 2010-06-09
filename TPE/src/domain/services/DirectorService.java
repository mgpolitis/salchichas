package domain.services;

import java.util.List;

import pdclogs.LogsClient;
import tgp.TGPServer;
import tmp.TMPServer;
import marshall.model.EndPoint;

public interface DirectorService extends ProtocolsMessageHandler {

	public TMPServer getTMPServer();
	
	public TGPServer getTGPServer();
	
	public LogsClient getLogsClient();
	
	public void startWorkingSession(EndPoint myEndPoint);

	public EndPoint getDirector();

	public List<EndPoint> getWorkers();

	public int getJobsDone();

	public int getLinesProcessed();

}
