package org.huanghobbs.networkframe;


/**
 * a generic event meant to contain whatever information you need to shove into it.
 * used to type the network and Game interactions.
 * 
 * it's basically just a box for variables
 * @author Maxwell
 *
 */
public abstract class GameEvent {
	
	public long eventTime;
	
	public GameEvent(long gameTime){
		this.eventTime=gameTime;
	}
	
}
