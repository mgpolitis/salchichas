package tmp;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import domain.services.WorkerService;

import tmp.TMPMessage;
import marshall.base.BaseServer;
import marshall.model.EndPoint;
import marshall.model.Message;


public class TMPServer extends BaseServer{

	WorkerService workerService;
	
	public static void main(String[] args) throws IOException {
	
	}
	
	public TMPServer(WorkerService workerService){
		super();
		this.workerService = workerService;
	}

	@Override
	public Message createMessage(byte[] serialized) {
		TMPMessage message = new TMPMessage(serialized);
		return message;
	}

	@Override
	public List<Message> messageReceived(Message m) {
		List<Message> list = new LinkedList<Message>();
		Message messageToSend = null;
		if (m instanceof TMPMessage) {
			System.out.println(m);
			TMPMessage message = (TMPMessage) m;
			System.out.println("Server: " + message);
			
			if (message.getType().equals("TMPREQUEST")) {
				messageToSend = requestMessage(message);
				
			} else {
				// TODO: unknown message
			}
			
		}
		if (messageToSend != null) {
			list.add(messageToSend);
		}
		return list;
	}
	
	private Message requestMessage(TMPMessage message) {
		String serviceRequest = message.getService();
		Message resp = null;
		
		if(!serviceRequest.isEmpty()){
			int service = Integer.valueOf(serviceRequest);		
		
		
			switch(service){
				case 1: //TODO: cantidad de lineas
					resp = lines();
					break;
				case 2: //TODO: cantidad de trabajos procesados
					resp = jobs();
					break;
				case 3: //TODO: listado de trabajadores suscriptos
					resp = workers();
					break;
				case 4: //TODO: nodo director
					resp = director();
					break;
			}
		}
		else{
			//TODO: Se pide todo.
			resp = all();
		}
		
		return resp;
	}
	
	private Message lines(){
		TMPMessage message = null;
		
		return message;
	}
	
	private Message jobs(){
		TMPMessage message = null;
		
		return message;
	}
	
	private Message workers(){
		TMPMessage message = null;
		
		return message;
	}
	
	private Message director(){
		TMPMessage message = null;
		
		return message;
	}
	
	private Message all(){
		TMPMessage message = null;
		
		return message;	
	}
	
}
