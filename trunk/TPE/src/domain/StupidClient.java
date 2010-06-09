package domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import marshall.Reactor;
import wdp.FrontEndWDPClient;

public class StupidClient {

	private static BufferedReader r = new BufferedReader(new InputStreamReader(
			System.in));

	public static void main(String[] args) throws IOException {

		String serverHost = secureRead(
				"Please enter the directors hostname or IP address:",
				"Please enter a valid hostname");

		Reactor reactor = Reactor.getInstance();
		FrontEndWDPClient wdpClient = new FrontEndWDPClient(serverHost,
				Configuration.WDP_SERVER_PORT);
		reactor.subscribeTCPClient(wdpClient, serverHost,
				Configuration.WDP_SERVER_PORT);
		reactor.run();

		String option = null;

	}

	private static String secureRead(String message, String errorMessage) {
		String rta = null;
		do {
			System.out.println(message);
			try {
				rta = r.readLine();
				if (rta == null) {
					System.out.println(errorMessage);
				}
			} catch (IOException e) {
				System.out.println("Error while reading from stdin");
				System.exit(1);
			}
		} while (rta == null);
		return rta;
	}

}
