package domain.services;

import pdclogs.LogsClient;
import tgp.TGPClient;
import domain.data.WorkerDAO;

public class WorkerServiceImpl implements WorkerService{

	private WorkerDAO workerDao;
	private TGPClient tgpClient = null;
	private LogsClient logsClient = null;
	
	public WorkerServiceImpl (WorkerDAO workerDao){
		this.workerDao = workerDao;
		tgpClient = new TGPClient("localhost",8092,this);
		logsClient = new LogsClient("localhost",8085,this);
	}

	@Override
	public void badRequest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logError(String error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveLogs(String logs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGroup(int group) {
		workerDao.setGroup(group);
	}
	
	@Override
	public String getWorkerHost(){
		return workerDao.getWorkerHost();
	}
	
	@Override
	public int getWorkerPort(){
		return workerDao.getWorkerPort();
	}

	public TGPClient getTgpClient() {
		return tgpClient;
	}

	public LogsClient getLogsClient() {
		return logsClient;
	}
	
	
	
	
	
}
