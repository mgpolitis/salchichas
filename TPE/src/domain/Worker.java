package domain;

import java.io.IOException;

import marshall.Reactor;
import domain.data.WorkerDAO;
import domain.services.WorkerService;
import domain.services.WorkerServiceImpl;

public class Worker extends Node{

	public static void main(String[] args) throws IOException {
		WorkerService workerService = new WorkerServiceImpl(new WorkerDAO());
		Reactor reactor = Reactor.getInstance();
		//TODO: consultar sobre el tema de porque hace falta poner aca y en el logsclient el serverhost y port
		reactor.subscribeTCPClient(workerService.getLogsClient(), "localhost", 8085);
		//reactor.subscribeTCPClient(workerService.getTgpClient(), "localhost", 8092);
		reactor.subscribeTCPServer(workerService.getWdpServer(), 8086);
		reactor.subscribeTCPServer(workerService.getTmpServer(), 8099);
		reactor.run();
	}
	
	
}
