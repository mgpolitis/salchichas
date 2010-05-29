package domain.data;

import pdclogs.LogsClient;
import tgp.TGPClient;


public class WorkerDAO {
	private String directorIP = null;
	private TGPClient tgpClient = null;
	private LogsClient logsClient = null;
	private StringBuffer logs =  null;
	
	public WorkerDAO(){
		logs = new StringBuffer();
		tgpClient = new TGPClient("localhost","8091","localhost","8092",this);
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
	
	
	
	
}
