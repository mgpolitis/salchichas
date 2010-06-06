package tmp;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import domain.Configuration;
import domain.services.DirectorServiceImpl;

import marshall.Reactor;
import marshall.base.BaseClient;
import marshall.model.EndPoint;
import marshall.model.Message;

public class TMPClient extends BaseClient{
	
	//private String tmpCliHost;
	//private int tmpCliPort;
	
	
	private static Pattern servicePattern = Pattern.compile("[0-9]+\\n");
	
	//public static void main(String[] args) throws IOException {
		
	//}
	
	public TMPClient(){
		super();
		//this.tmpCliHost = tmpCliHost;
		//this.tmpCliPort = tmpCliPort;
		
	}

	@Override
	public Message createMessage(byte[] serialized) {
		TMPMessage message = new TMPMessage(serialized);
		return message;
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
		
		//message.origin = new EndPoint(this.tmpCliHost, this.tmpCliPort);
		
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
		//TODO: recibe el mensaje con la respuesta.
		List<Message> list = null;
		//Message messageToSend = null;
		EndPoint endPoint = null;
		
		if (m instanceof TMPMessage) {
			TMPMessage message = (TMPMessage) m;
			
			
			// pedidos recursivos a los workers
			String workers = message.getWorkers();
			if(!workers.isEmpty()){
				System.out.println("=>");
				String[] tempIPHArray = workers.split("|");
				for(String str: tempIPHArray){
					String[] tempArray = str.split("-");
					// hago una conexión hacia cada uno de los ip:puerto
					if( tempArray.length == 2 ){
						int port = Integer.valueOf(tempArray[1]);
						try{
							Reactor.getInstance().subscribeTCPClient(new TMPClient(), tempArray[0], Configuration.TMP_SERVER_PORT);
						}catch(IOException e){
							//TODO: manejo
						}
					}
				}
			}
			
		}
		System.out.println("CLIENT: " + m);
		
		return list;
	}
	
}
