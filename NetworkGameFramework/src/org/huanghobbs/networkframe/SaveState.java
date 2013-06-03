package org.huanghobbs.networkframe;

public abstract class SaveState{
	
	public long gameTime;
	GameTimer targetGame;
	
	public SaveState(GameTimer target){
		this(target,target.currentTime());
	}
	
	public SaveState(GameTimer target, long time){
		this.gameTime=target.currentTime();
		this.targetGame=target;
	}
	
	public abstract void restore();

}
