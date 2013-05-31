package org.huanghobbs.networkframe;

public class GameTimer {

	long startTime;
	
	public void start(){
		this.startTime = System.currentTimeMillis();
	}
	
	public long currentTime(){
		return System.currentTimeMillis()-startTime;
	}
	
}
