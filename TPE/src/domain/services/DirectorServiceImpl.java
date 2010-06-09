package domain.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import marshall.Reactor;
import marshall.model.EndPoint;
import pdclogs.LogsClient;
import tgp.TGPServer;
import tmp.TMPServer;
import wdp.WDPClient;
import wdp.WDPServer;
import domain.Configuration;
import domain.data.DirectorDAO;

public class DirectorServiceImpl implements DirectorService{

	private DirectorDAO directorDao;
	private Map<EndPoint, WDPClient> wdpClients = null;
	private Set<EndPoint> workersBusy = null;
	private Set<EndPoint> workersIdle = null;
	private WDPServer wdpServer = null;
	private TMPServer tmpServer = null;
	private TGPServer tgpServer = null;
	private LogsClient logsClient = null;
	private List<String> countries;
	private List<String> userAgents;
	private String datesParam;
	private int lines;
	private int contentLength;

	public DirectorServiceImpl(DirectorDAO directorDao) {
		this.directorDao = directorDao;
		wdpClients = new HashMap<EndPoint, WDPClient>();
		workersBusy = new HashSet<EndPoint>();
		workersIdle = new HashSet<EndPoint>();
		wdpServer = new WDPServer(this);
		// tmpServer = new TMPServer(workerService);
		tgpServer = new TGPServer("1", "localhost",
				Configuration.TGP_SERVER_PORT, this);

	}


	@Override
	public void startWorkingSession(EndPoint myEndPoint) {
		WDPClient client = new WDPClient(myEndPoint.host, myEndPoint.port,this);
		Reactor reactor = Reactor.getInstance();
		try {
			reactor
					.subscribeTCPClient(client, myEndPoint.host,
							myEndPoint.port);
			wdpClients.put(myEndPoint, client);
			workersBusy.add(myEndPoint);
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
		try {
			this.directorDao.setResource(resource);
			this.directorDao.setLogsServer(new EndPoint(hostname, port));
			logsClient.fetchResourceInfo(resource, hostname, port);
		} catch (IOException e) {
			// TODO: enviar mensaje de error a quien corresponda
			e.printStackTrace();
		}
	}

	@Override
	public void setParamsToProcess(List<String> countries,
			List<String> userAgents, String datesParam) {
		this.countries = countries;
		this.userAgents = userAgents;
		this.datesParam = datesParam;
	}

	@Override
	public void saveLogs(String logs) {
		return;
	}

	@Override
	public void saveResourceInfo(String lines, String contentLength) {
		this.lines = Integer.valueOf(lines);
		this.contentLength = Integer.valueOf(contentLength);
		try {
			distributeWork();
		} catch (IOException e) {
			// TODO hacer algo con el error
			e.printStackTrace();
		}
	}

	private void distributeWork() throws IOException {
		int workersQty = this.workersIdle.size();
		String resource = this.directorDao.getResource();
		if (workersQty > 0) {
			int linesPerWorker = lines / workersQty;
			int counter = getMinRange(resource);
			Iterator<EndPoint> it = this.workersIdle.iterator();
			
			while(it.hasNext()){
				EndPoint endPoint = it.next();
				WDPClient client = this.wdpClients.get(endPoint);
				it.remove();
				this.workersBusy.add(endPoint);
				client.getJobDone(endPoint, getNakedResource(resource)+"?"+counter+"-"+(counter+linesPerWorker-1), userAgents, countries, datesParam);
			}
		}
	}
	
	private int getMinRange(String resource){
		String aux[] = resource.split("?");
		if(aux.length > 1){
			String ranges[] = aux[1].split("-");
			if(ranges.length > 1){
				return Integer.valueOf(ranges[0].trim());
			}
		}
		return 0;
	}

	private int getMaxRange(String resource){
		String aux[] = resource.split("?");
		if(aux.length > 1){
			String ranges[] = aux[1].split("-");
			if(ranges.length > 1){
				return Integer.valueOf(ranges[1].trim());
			}
		}
		return 0;
	}
	
	
	private static final Pattern nakedResourcePattern = Pattern.compile("(/[^\\?]*)");
	private String getNakedResource(String resource){
		String aux[] = resource.split("/");
		if(aux.length > 3){
			Matcher m = nakedResourcePattern.matcher(aux[3]);
			if(m.find()){
				return m.group(1);
			}
		}
		return "";
	}


	@Override
	public EndPoint getDirector() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<EndPoint> getWorkers() {
		return (List<EndPoint>) this.wdpClients.keySet();
	}


	@Override
	public int getJobsDone() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getLinesProcessed() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void notifyWorkEnd(Map<String, Integer> results, EndPoint worker) {
		if(this.wdpClients.get(worker) != null){
			this.workersBusy.remove(worker);
			this.workersIdle.add(worker);
		}
		Map<String,Integer> map = this.directorDao.getDataProccesed();
		if(results.get("HITS") != null){
			map.put("HITS", map.get("HITS")+results.get("HITS"));
		}
		if(results.get("BYTES") != null){
			map.put("BYTES", map.get("BYTES")+results.get("BYTES"));
		}
	}

}
