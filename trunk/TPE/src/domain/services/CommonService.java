package domain.services;

import marshall.model.EndPoint;

public class CommonService {
	private EndPoint director;
	private int jobsDone;
	private int linesProcessed;
	private Type mytype;
	public enum Type {DIRECTOR,WORKER};
	
	public CommonService(Type type){
		jobsDone = 0;
		linesProcessed = 0;
		mytype = type;
		director = null;
	}
	
	public Type getMytype() {
		return mytype;
	}
	public void setMytype(Type mytype) {
		this.mytype = mytype;
	}
		
	public EndPoint getDirector() {
		return director;
	}
	public void setDirector(EndPoint director) {
		this.director = director;
	}
	public int getJobsDone() {
		return jobsDone;
	}
	public void setJobsDone(int jobsDone) {
		this.jobsDone = jobsDone;
	}
	public int getLinesProcessed() {
		return linesProcessed;
	}
	public void setLinesProcessed(int linesProcessed) {
		this.linesProcessed = linesProcessed;
	}
	
	
}
