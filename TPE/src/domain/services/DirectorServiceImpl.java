package domain.services;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import marshall.Reactor;
import marshall.model.EndPoint;
import pdclogs.LogsClient;
import tgp.TGPServer;
import tmp.TMPServer;
import wdp.WDPClient;
import wdp.WDPServer;
import domain.Configuration;
import domain.data.DirectorDAO;


public class DirectorServiceImpl implements DirectorService {

	private DirectorDAO directorDao;
	private Map<EndPoint,WDPClient> wdpClients = null;
	private WDPServer wdpServer = null;
	private TMPServer tmpServer = null;
	private TGPServer tgpServer = null;
	private LogsClient logsClient = null;
	
	public DirectorServiceImpl (DirectorDAO directorDao){
		this.directorDao = directorDao;
		wdpClients = new HashMap<EndPoint,WDPClient>();
		wdpServer = new WDPServer(this);
		//tmpServer = new TMPServer(workerService);
		tgpServer = new TGPServer("1", "localhost", Configuration.TGP_SERVER_PORT, this);
		
	}

	@Override
	public void addWorker(EndPoint workerEndPoint) {
		this.directorDao.addWorker(workerEndPoint);
		
	}

	@Override
	public void startWorkingSession(EndPoint myEndPoint) {
		WDPClient client = new WDPClient(myEndPoint.host, myEndPoint.port);
		Reactor reactor = Reactor.getInstance();
		try {
			reactor.subscribeTCPClient(client, myEndPoint.host, myEndPoint.port);
			wdpClients.put(myEndPoint, client);
		} catch (IOException e) {
			// TODO ver porque no se puede agregrar el worker
			System.out.println("no se puede agregar el worker en el director");
			e.printStackTrace();
		}
	}

	@Override
	public LogsClient getLogsClient() {
		return logsClient;
	}

	@Override
	public TGPServer getTGPServer() {
		return tgpServer;
	}

	@Override
	public TMPServer getTMPServer() {
		return tmpServer;
	}

	@Override
	public void fetchResource(String resource, String hostname, int port) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParamsToProcess(List<String> countries,
			List<String> userAgents, String datesParam) {
		// TODO Auto-generated method stub
		
	}

	
	
}
