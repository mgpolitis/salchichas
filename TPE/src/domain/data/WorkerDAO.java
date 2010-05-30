package domain.data;

import pdclogs.LogsClient;
import tgp.TGPClient;


public class WorkerDAO {
	private String directorIP = null;
	private StringBuffer logs =  null;
	private String host;
	private int port;
	private int group;
	
	public WorkerDAO(){
		logs = new StringBuffer();
	}

	public String getLogs() {
		return logs.toString();
	}

	public void setLogs(String logs) {
		this.logs.delete(0, this.logs.length());
		this.logs.append(logs);
	}
	
	public String getWorkerHost(){
		return host;
	}
	
	public int getWorkerPort(){
		return port;
	}

	public void setGroup(int group) {
		this.group = group;
	}
	
	public int getGroup(){
		return group;
	}
	
}
