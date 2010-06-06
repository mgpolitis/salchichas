package tmp;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import domain.data.WorkerDAO;

import marshall.interfaces.BaseClient;
import marshall.model.EndPoint;
import marshall.model.Message;

public class TMPClient implements BaseClient{
	
	private String tmpCliHost;
	private int tmpCliPort;
	private WorkerDAO workerDao;
	
	private static Pattern servicePattern = Pattern.compile("[0-9]+\\n");
	
	//public static void main(String[] args) throws IOException {
		
	//}
	
	public TMPClient(String tmpCliHost, int tmpCliPort, WorkerDAO workerDao){
		super();
		this.tmpCliHost = tmpCliHost;
		this.tmpCliPort = tmpCliPort;
		this.workerDao = workerDao;
	}

	@Override
	public Message createMessage(byte[] serialized) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message greet() {
		String messageContent = null;
				
		List<String> c = new ArrayList<String>();
		
		String input = readMessage();
		
		Matcher m = servicePattern.matcher(input);
		if (m.find()) {
			messageContent = "service: " + m.group(1);
			c.add(messageContent);
		} else {
			System.out.println("No service specified");
		}
				
		TMPMessage message = new TMPMessage("TMPREQUEST",c);
		
		message.origin = new EndPoint(this.tmpCliHost, this.tmpCliPort);
		
		System.out.println("Message Sent to Server: "+message);
		return message;
	}

	private String readMessage() {
		StringBuffer aux = new StringBuffer();
		BufferedReader stdin = new BufferedReader(new InputStreamReader(
				System.in));
		
		String line;
		try {
			do {
				line = stdin.readLine();
				aux.append(line);
				aux.append('\n');
			}
			while(line != null && !line.isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return aux.toString();
	}
	

	@Override
	public List<Message> messageReceived(Message m) {
		List<Message> list = new LinkedList<Message>();
		
		//TODO: recibe el mensaje con la respuesta.
		
		return list;
	}
	
}
