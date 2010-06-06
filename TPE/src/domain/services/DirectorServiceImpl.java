package domain.services;


import tmp.TMPServer;
import wdp.WDPServer;
import domain.data.DirectorDAO;


public class DirectorServiceImpl implements DirectorService{

	private DirectorDAO directorDao;
		
	public DirectorServiceImpl (DirectorDAO directorDao){
		this.directorDao = directorDao;
		
	}

	@Override
	public WDPServer getWdpServer() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
