package domain.data;


public class DirectorDAO {
	private String directorIP = null;
	private StringBuffer logs = null;

	private String host;
	private int port;

	private int group;


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


}
