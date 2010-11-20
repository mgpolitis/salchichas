package ar.edu.itba.pod.legajo49244.main;

import java.io.IOException;
import java.io.Serializable;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

import com.google.common.base.Preconditions;

public class MySocketFactory extends RMISocketFactory implements Serializable {

	private static final long serialVersionUID = -6333877561510509023L;
	InetAddress ipInterface = null;

	public MySocketFactory(InetAddress ipInterface) {
		this.ipInterface = ipInterface;
	}

	public ServerSocket createServerSocket(int port) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port, 50, ipInterface);
		} catch (BindException e) {
			System.out.println("FATAL: Could not bind to address "+ipInterface);
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return serverSocket;
	}

	@Override
	public Socket createSocket(String arg0, int arg1) throws IOException {
		Preconditions.checkArgument(false);
		return null;
	}

}