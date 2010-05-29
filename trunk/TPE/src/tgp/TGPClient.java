package tgp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InterfaceAddress;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import marshall.ClientReactor;
import marshall.interfaces.BaseClient;
import marshall.model.EndPoint;
import marshall.model.Message;

public class TGPClient implements BaseClient{
	
	private int group;
	private String workerHost;
	private String workerPort;
	private String tgpCliHost;
	private String tgpCliPort;
	
	private State state;
	private String xid;
	
	private static final int serverPort = 8092;
	private static Pattern groupPattern = Pattern.compile("[0-9]+\\n");
	private enum State { WAITING_OFFER, WAITING_ACK, SUBSCRIBED };
	
	public TGPClient(String workerHost, String workerPort, String tgpCliHost, String tgpCliPort){
		super();
		this.workerHost = workerHost;
		this.workerPort = workerHost;
		this.tgpCliHost = tgpCliHost;
		this.tgpCliPort = tgpCliPort;
	}
	
	@Override
	public Message createMessage(byte[] serialized) {
		TGPMessage message = new TGPMessage(serialized);
		return message;
	}

	@Override
	public Message greet() {
		// TGPDISCOVER
		
		String messageContent = null;
		xid = null;
		
		List<String> c = new ArrayList<String>();
		
		String input = readMessage();
		
		Matcher m = groupPattern.matcher(input);
		if (m.find()) {
			messageContent = "group: " + m.group(1);
			c.add(messageContent);
		} else {
			System.out.println("No group specified");
		}
		
				
		TGPMessage message = new TGPMessage("TGPDISCOVER",c);
		
		state = State.WAITING_OFFER;
		
		message.broadcastMe=true;
		message.origin = new EndPoint(this.tgpCliHost, Integer.valueOf(this.tgpCliPort));
		
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
		Message messageToSend = null;
		if (m instanceof TGPMessage) {
			TGPMessage message = (TGPMessage) m;
			System.out.println("CLIENT: " + message);
			
			if(message.getType().equals("TGPOFFER") && state == State.WAITING_OFFER){
				messageToSend = offerReceived(message);
			}
			else if(message.getType().equals("TGPACK")){
				if(state==State.WAITING_ACK){
					state=State.SUBSCRIBED;
				} //No hay respuesta al ACK
			}
			else {
				//unknown message format
			}
			
			if(messageToSend !=null){
				list.add(messageToSend);				
			}
			
		}
		
		return list;
	}

	
	private Message offerReceived(TGPMessage message) {
		List<String> content = new ArrayList<String>();
		String respGroup = message.getGroup();
		this.xid = message.getXid();
		
		if(respGroup.isEmpty() || xid.isEmpty()){
			return null; //Mensaje mal formado.
		}
		content.add("group: " + respGroup);
		content.add("host: " + workerHost);
		content.add("port: "+ workerPort);
		content.add("xid: " + xid);
		this.group=Integer.valueOf(respGroup);
		
		TGPMessage messageToSend = new TGPMessage("TGPREQUEST", content);
		
		messageToSend.broadcastMe=true;
		messageToSend.origin = new EndPoint(this.tgpCliHost, Integer.valueOf(this.tgpCliPort));
		return messageToSend;
	}

	public static void main(String[] args) throws IOException {
		ClientReactor reactor = ClientReactor.getInstance();
		// worker host port + tgp host port
		// lo utiliza para indicarle al servidor a donde va a tener que conectarse el director
		TGPClient c = new TGPClient("localhost","8091","localhost","8092");
		reactor.subscribeTCPClient(c, "localhost", 8092);
		reactor.runClient();
	}
	
}
