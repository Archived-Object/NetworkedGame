package org.huanghobbs.networkframe;

public abstract class GameTimer {

	long startTime;
	protected long lastTick;
	protected long maxRollback = 1000;

	
	public void start(){
		this.startTime = System.currentTimeMillis();
	}
	
	public long currentTime(){
		return System.currentTimeMillis()-startTime;
	}
	
}
