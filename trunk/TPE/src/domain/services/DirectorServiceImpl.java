package domain.services;


import marshall.model.EndPoint;
import domain.data.DirectorDAO;


public class DirectorServiceImpl implements DirectorService{

	private DirectorDAO directorDao;
		
	public DirectorServiceImpl (DirectorDAO directorDao){
		this.directorDao = directorDao;
		
	}

	@Override
	public void addWorker(EndPoint workerEndPoint) {
		this.directorDao.addWorker(workerEndPoint);
		
	}

	@Override
	public void startWorkingSession(EndPoint myEndPoint) {
		// TODO Auto-generated method stub
		
	}

	
	
}
