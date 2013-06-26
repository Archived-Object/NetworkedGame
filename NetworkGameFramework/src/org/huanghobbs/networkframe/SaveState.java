package org.huanghobbs.networkframe;

import org.huanghobbs.networkframe.client.ClientSimulation;

public abstract class SaveState{
	
	public long gameTime;
	public boolean isClient;
	protected GameTimer targetGame;
	
	public SaveState(GameTimer target){
		this(target,target.currentTime());
		isClient = target.getClass().isAssignableFrom(ClientSimulation.class);
	}
	
	public SaveState(GameTimer target, long time){
		this.gameTime=target.currentTime();
		this.targetGame=target;
	}
	
	public void restore(){
		if(isClient){
			restoreToClient();
		} else{
			restoreToServer();
		}
	}
	
	public abstract void restoreToServer();
	public abstract void restoreToClient();

	@Override
	public String toString(){
		return new StringBuilder().append("<SaveState: ").append(gameTime).append(">").toString();
	}
	
}
