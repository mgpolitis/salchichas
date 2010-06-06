package wdp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marshall.base.BaseServer;
import marshall.model.EndPoint;
import marshall.model.Message;
import domain.services.WorkerService;

public class WDPServer extends BaseServer {
	

	private final WorkerService workerService;
	private EndPoint client = null;
	
	public WDPServer(WorkerService workerService){
		super();
		this.workerService = workerService;
	}
	
	@Override
	public List<Message> messageReceived(Message m) {
		List<Message> list = new LinkedList<Message>();
		WDPMessage messageToSend = null;
		if (m instanceof WDPMessage) {
			WDPMessage message = (WDPMessage) m;
			if (message.getType().equals("PROCESS")) {
				client = message.origin;
				EndPoint endPoint = message.getEndPoint();
				String userAgentsString = message.getHeader("user-agents");
				String countriesString = message.getHeader("countries");
				String dates = message.getHeader("dates");
				List<String> userAgents = null;
				List<String> countries = null;
				if(userAgentsString != null){
					userAgents = Arrays.asList(userAgentsString.split(";"));
				}
				if(countriesString != null){
					countries = Arrays.asList(countriesString.split(";"));
				}
				workerService.setParamsToProcess(countries, userAgents, dates); 
				workerService.fetchResource(message.getFilename(),endPoint.host,endPoint.port);
			} else {
				// TODO: unknown message
			}
		}
		if (messageToSend != null) {
			list.add(messageToSend);
		}
		return list;
	}


	@Override
	public Message createMessage(byte[] serialized) {
		WDPMessage message = new WDPMessage(serialized);
		return message;
	}
	
	public void sendWorkDone(Map<String,Integer> result) throws IOException{
		List<String> headers = new ArrayList<String>();
		headers.add("HITS: "+result.get("hits"));
		headers.add("BYTES: "+result.get("bytes"));
		WDPMessage messageToSend  = new WDPMessage("WORKDONE", headers, null);
		messageToSend.setDest(client);
		this.sendMessage(messageToSend);
	}
//	
//	public static void main(String[] args) throws IOException {
//		Reactor reactor = Reactor.getInstance();
//		WDPServer s = new 	WDPServer("./logfiles");
//		reactor.subscribeTCPServer(s, 8085);
//		reactor.run();
//	}
}
