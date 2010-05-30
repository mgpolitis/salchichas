package domain.data;

import pdclogs.LogsClient;
import tgp.TGPClient;


public class WorkerDAO {
	private String directorIP = null;
	private TGPClient tgpClient = null;
	private LogsClient logsClient = null;
	private StringBuffer logs =  null;
	private String host;
	private int port;
	private int group;
	
	public WorkerDAO(){
		logs = new StringBuffer();
		tgpClient = new TGPClient("localhost",8092,this);
		logsClient = new LogsClient("localhost",8085,this);
	}

	public StringBuffer getLogs() {
		return logs;
	}

	public void setLogs(StringBuffer logs) {
		this.logs = logs;
	}

	public TGPClient getTgpClient() {
		return tgpClient;
	}

	public void setTgpClient(TGPClient tgpClient) {
		this.tgpClient = tgpClient;
	}

	public LogsClient getLogsClient() {
		return logsClient;
	}

	public void setLogsClient(LogsClient logsClient) {
		this.logsClient = logsClient;
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
