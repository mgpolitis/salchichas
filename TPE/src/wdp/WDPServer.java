package wdp;

import java.util.LinkedList;
import java.util.List;

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
		Message messageToSend = null;
		if (m instanceof WDPMessage) {
			WDPMessage message = (WDPMessage) m;
			if (message.getType().equals("PROCESS")) {
				//TODO: pedir los logs al server de logs
				//TODO: procesar el request
				
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
