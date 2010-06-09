package domain.data;

import java.util.HashMap;
import java.util.Map;

import marshall.model.EndPoint;


public class DirectorDAO {
	private String directorIP = null;
	private StringBuffer logs = null;

	private String host;
	private int port;

	private int group;
	
	
	private String resource;
	private EndPoint logsServer;
	
	private Map<String,Integer> dataProccesed = new HashMap<String,Integer>();

	public DirectorDAO() {
		logs = new StringBuffer();
		dataProccesed.put("HITS", 0);
		dataProccesed.put("BYTES", 0);
	}
	
	
	public Map<String, Integer> getDataProccesed() {
		return dataProccesed;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public EndPoint getLogsServer() {
		return logsServer;
	}

	public void setLogsServer(EndPoint logsServer) {
		this.logsServer = logsServer;
	}


	public String getDirectorHost() {
		return host;
	}

	public int getDirectorPort() {
		return port;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public int getGroup() {
		return group;
	}


}
