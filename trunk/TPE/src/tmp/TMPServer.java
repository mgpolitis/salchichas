package tmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import domain.services.DirectorService;
import domain.services.WorkerService;

import tmp.TMPMessage;

import marshall.base.BaseServer;
import marshall.model.EndPoint;
import marshall.model.Message;



public class TMPServer extends BaseServer{

	DirectorService directorService;
	WorkerService workerService;
	
	public static void main(String[] args) throws IOException {
	
	}
	
	public TMPServer(DirectorService directorService){
		super();
		this.directorService = directorService;
		this.workerService = null;
		
	}

	public TMPServer(WorkerService workerService){
		super();
		this.directorService = null;
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
		TMPMessage resp = null;
		
		if(!serviceRequest.isEmpty()){
			int service = Integer.valueOf(serviceRequest);		
		
		
			switch(service){
				case 1: 
					resp = lines(resp);
					break;
					
				case 2: 
					resp = jobs(resp);
					break;
					
				case 3: 
					resp = workers(resp);
					break;
					
				case 4: 
					resp = director(resp);
					break;
					
			}
		}
		else{
			resp = all(resp);
		}
		
		if(directorService!=null){
			Collection<String> c = new ArrayList<String>();
			//si soy director agrego la informacion para de mis hijos
			//TODO: List workers = this.directorService.getActiveWorkers();
			
			List<EndPoint> workers = null;
			
			String myWorkers = "";
			
			for(EndPoint ep : workers){
				myWorkers += ep.host + "-" + ep.port;
			}
			
			c.add(myWorkers);
			resp.addContents(c);
		}
		
		
		return resp;
	}
	
	private TMPMessage lines(TMPMessage message){
		
		//TODO: this.workerService.getLinesProcessed();
		
		return message;
	}
	
	private TMPMessage jobs(TMPMessage message){
		
		//TODO:this.workerService.getJobsDone();	
		
		return message;
	}
	
	private TMPMessage workers(TMPMessage message){
		
		
		return message;
	}
	
	private TMPMessage director(TMPMessage message){
		
		
		//TODO:this.workerService.getDirector();
		
		return message;
	}
	
	private TMPMessage all(TMPMessage message){
		// MMM!!!!
		message = lines(message);
		message = jobs(message);
		message = workers(message);
		message = director(message);
		
		return message;	
	}
	
}
