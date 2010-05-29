package domain;

import java.util.HashSet;
import java.util.Set;

public class Director extends Node {
	Set<Node> workers = null;
	
	public Director(){
		this.workers = new HashSet<Node>();
	}
}
