package domain.data;

import java.util.HashSet;
import java.util.Set;

import marshall.model.EndPoint;

public class DirectorDAO {
	private String directorIP = null;
	private StringBuffer logs = null;

	private String host;
	private int port;

	private int group;

	private final Set<EndPoint> workers = new HashSet<EndPoint>();

	public DirectorDAO() {
		logs = new StringBuffer();
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

	public void addWorker(EndPoint workerEndPoint) {
		this.workers.add(workerEndPoint);

	}

}
