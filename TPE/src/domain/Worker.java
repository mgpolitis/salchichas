package domain;

import java.io.IOException;

import marshall.Reactor;
import tgp.TGPClient;
import domain.data.WorkerDAO;

public class Worker extends Node{

	public static void main(String[] args) throws IOException {
		WorkerDAO wd = new WorkerDAO();
		Reactor reactor = Reactor.getInstance();
		TGPClient c = new TGPClient("localhost","8091","localhost","8092",wd);
		reactor.subscribeTCPClient(c, "localhost", 8092);
		reactor.run();
	}
	
	
}
