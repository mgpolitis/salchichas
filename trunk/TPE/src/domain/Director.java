package domain;

import java.io.IOException;

import marshall.Reactor;
import domain.data.DirectorDAO;
import domain.services.DirectorService;
import domain.services.DirectorServiceImpl;


public class Director extends Node {
	public static void main(String[] args) throws IOException {
		DirectorService directorService = new DirectorServiceImpl(new DirectorDAO());
		Reactor reactor = Reactor.getInstance();
		reactor.subscribeTCPClient(directorService.getLogsClient(), "localhost", 8085);
		reactor.subscribeUDPServer(directorService.getTGPServer(), 8092);
		reactor.subscribeTCPServer(directorService.getTMPServer(), 8086);
		reactor.run();
	}
}
