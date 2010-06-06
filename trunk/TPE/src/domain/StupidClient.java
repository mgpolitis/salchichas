package domain;

import java.io.IOException;

import marshall.Reactor;
import wdp.FrontEndWDPClient;

public class StupidClient {

	public static void main(String[] args) throws IOException {
		Reactor reactor = Reactor.getInstance();

		String serverHost = null; // TODO: obtener de linea de comando
		FrontEndWDPClient c = new FrontEndWDPClient(serverHost,
				Configuration.WDP_SERVER_PORT);
		reactor.subscribeTCPClient(c, "localhost",
				Configuration.WDP_SERVER_PORT);
		reactor.run();
	}

}
