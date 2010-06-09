package tmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import domain.services.CommonService;
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
					resp = lines();
					break;
					
				case 2: 
					resp = jobs();
					break;
					
				case 3: 
					resp = workers();
					break;
					
				case 4: 
					resp = director();
					break;
					
			}
		}
		else{
			resp = all();
		}
		
		if(directorService!=null){
			Collection<String> c = new ArrayList<String>();
			//si soy director agrego la informacion para de mis hijos
			
			List<EndPoint> workers = this.directorService.getWorkers();
			
			String myWorkers = "workers: ";
			
			EndPoint ep = workers.get(0);
			myWorkers +=  ep.host + "-" + ep.port ;
			for( EndPoint ep1 : workers){
				myWorkers += "|" + ep1.host + "-" + ep1.port ;
			}
			
			c.add(myWorkers);
			resp.addContents(c);
		}
		
		
		return resp;
	}
	
	private TMPMessage lines(){
		List<String> content = new ArrayList<String>();
		String str = "";
		
		if(workerService!=null){
			str = "lines: " + this.workerService.getLinesProcessed();
		}
		else if(directorService!=null){
			str = "lines:" +  this.directorService.getLinesProcessed();
		}
		content.add(str);
		TMPMessage message = new TMPMessage("TMPRESPONSE",content);
		return message;
	}
	
	private TMPMessage jobs(){
		List<String> content = new ArrayList<String>();
		String str = "";
		
		if(workerService!=null){
			str = "lines: " + this.workerService.getJobsDone();
		}
		else if(directorService!=null){
			str = "lines:" +  this.directorService.getJobsDone();
		}
		content.add(str);
		TMPMessage message = new TMPMessage("TMPRESPONSE",content);
		return message;
	}
	
	private TMPMessage workers(){
		List<String> content = new ArrayList<String>();
		String str = "";
		
		if(workerService!=null){
			// Soy worker. No me pidas workers...
			str = "workers: " ;
		}
		else if(directorService!=null){
			// se agrega afuera
		}
		content.add(str);
		TMPMessage message = new TMPMessage("TMPRESPONSE",content);
		return message;
	}
	
	private TMPMessage director(){
		List<String> content = new ArrayList<String>();
		String str = "";
		EndPoint ep = null;
		if(workerService!=null){
			ep = this.workerService.getDirector();
			str = "director: " + ep.host + "-" + ep.port;
		}
		else if(directorService!=null){
			ep = this.directorService.getDirector();
			str = "director: " + ep.host + "-" + ep.port;
		}
		content.add(str);
		TMPMessage message = new TMPMessage("TMPRESPONSE",content);
		return message;
	}
	
	private TMPMessage all(){
		List<String> content = new ArrayList<String>();
		String linesStr = "";
		String workersStr = "";
		String directorStr = "";
		String jobsStr = "";
		EndPoint ep = null;
		if(workerService!=null){
			workersStr = "workers: " ;
			ep = this.workerService.getDirector();
			linesStr = "lines: " + this.workerService.getLinesProcessed();
			jobsStr = "jobs: " + this.workerService.getJobsDone();
			directorStr = "director: " + ep.host + "-" + ep.port;
			content.add(workersStr);
		}
		else if(directorService!=null){
			ep = this.directorService.getDirector();
			directorStr = "director: " + ep.host + "-" + ep.port;
			linesStr = "lines: " + this.directorService.getLinesProcessed();
			jobsStr = "jobs: " + this.directorService.getJobsDone();
			
			
		}
		
		content.add(directorStr);
		content.add(jobsStr);
		content.add(linesStr);
		
		TMPMessage message = new TMPMessage("TMPRESPONSE",content);
		return message;	
	}
	
}
