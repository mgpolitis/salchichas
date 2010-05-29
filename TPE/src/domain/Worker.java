package domain;

import java.io.IOException;

import marshall.Reactor;
import domain.data.WorkerDAO;

public class Worker extends Node{

	public static void main(String[] args) throws IOException {
		WorkerDAO wd = new WorkerDAO();
		Reactor reactor = Reactor.getInstance();
		reactor.subscribeTCPClient(wd.getLogsClient(), "localhost", 8085);
		//TODO: consultar sobre el tema de porque hace falta poner aca y en el logsclient el serverhost y port
		reactor.subscribeTCPClient(wd.getTgpClient(), "localhost", 8092);
		
		reactor.run();
	}
	
	
}
