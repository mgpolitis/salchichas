package wdp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marshall.base.BaseServer;
import marshall.model.Message;
import domain.services.WorkerService;

public class WDPServer extends BaseServer {
	

	private final WorkerService workerService;
	
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
				Map<String,Integer> result = workerService.processLogs(countries, userAgents, dates);
				List<String> headers = new ArrayList<String>();
				headers.add("HITS: "+result.get("hits"));
				headers.add("BYTES: "+result.get("bytes"));
				messageToSend = new WDPMessage("WORKDONE", headers, null); 
			} else {
				// TODO: unknown message
			}
			messageToSend.setDest(m.origin);
			messageToSend.setOrigin(m.dest);
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
//	
//	public static void main(String[] args) throws IOException {
//		Reactor reactor = Reactor.getInstance();
//		WDPServer s = new 	WDPServer("./logfiles");
//		reactor.subscribeTCPServer(s, 8085);
//		reactor.run();
//	}
}
