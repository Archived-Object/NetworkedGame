package org.huanghobbs.networkframe.server;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.huanghobbs.networkframe.GameEvent;
import org.huanghobbs.networkframe.GameTimer;


/**
 * controls gameplay (server side) to dispatch gameplay events
 * also checks the validity of player-controlled gameplay events
 * 
 * @author Maxwell
 *
 */
public abstract class ServerGameplay<G extends GameEvent> extends GameTimer{
	
	/** Static variable to control game universe "tick" speed*/
	protected int tickTime = 30;
	
	/*things that are for this class only*/
	protected ServerNetwork<G> network;
	protected Timer gameTimer;
	protected boolean tickingFlag = false;
	
	/** convenience variable, elapsed milliseconds since last tick*/
	protected long lastTick=0;
	
	/* communicate with outside this class*/
	public boolean initialized = false;

	protected LinkedList<GameEvent> eventRecord = new LinkedList<GameEvent>();
	
	/**
	 * creates a new ServerGameplay and ServerNetwork, then associates the two.
	 */
	public ServerGameplay(){
		this.network = new ServerNetwork<G>();
		this.network.setGame(this);
	}
	
	@Override
	public void start(){
		super.start();
		if(!this.network.started){
			this.network.start();
		}
		this.initialized=true;
		
		this.gameTimer = new Timer();
		this.gameTimer.scheduleAtFixedRate(new TickTimer<G>(this), 0, tickTime);
	}
	
	
	//GAMEPLAY content
	

	public boolean handleEventWrapped(G e, WrappedClient<G> source){
		synchronized(this.eventRecord){
			if(e.eventTime<this.lastTick &&
					(this.lastTick-e.eventTime<maxRollback || maxRollback==-1) ){
				long currentTime = this.lastTick;//if you can, roll back and apply the event from the server, then roll forward again
				this.rollbackTo(e.eventTime);
				boolean x = this.handleEvent(e,source);
				this.tickForwardTo(currentTime);
				return x;
			}
			else if (e.eventTime>this.lastTick && e.eventTime<=this.currentTime()){
				//only trust client to roll forward if the server hasn't updated in a whole)
				this.tickForwardTo(e.eventTime);
				return this.handleEvent(e,source);
			}
		}
		return false;
	}
	
	/**
	 * check if an incoming event is valid (allowed by game)
	 * if it is valid, parse the event and return true
	 * otherwise, (return false), the serverNetwork will say that the client committed some illegal action
	 * 
	 * @param e the event that is to be checked/implemented
	 * @return if the event was valid
	 */
	public abstract boolean handleEvent(G e, WrappedClient<G> source);
	

	public abstract void rollbackTo(long targetGameTime);
	public void tickForwardTo(long targetGameTime){//TODO recorded events
		long diff = targetGameTime-lastTick;
		for( int i=0; i<diff/this.tickTime; i++){
			this.tickUniverse(this.tickTime);
		}
		if(diff%tickTime!=0){
			this.tickUniverse(diff%this.tickTime);
		}
	}
	
	/**
	 * advances the universe of the game.
	 * should be called at the beginning of all subclasses' tickUniverse methods
	 * (does the elapsed calculations)
	 */
	public void tickUniverse(){
		long p = this.currentTime();
		long elapsed = (int)(p-this.lastTick);
		this.lastTick = p;
		tickUniverse(elapsed);
	}
	
	public abstract void tickUniverse(long elapsed);

	public abstract void onConnect(WrappedClient<G> justConnected);
	public abstract void onDisconnect(WrappedClient<G> justDropped);
	public abstract void onReconnect(WrappedClient<G> justConnected);
	
}


/**
 * timer task that tells the ServerGameplay to tick forward
 * tries to avoid overlapping ticks (make more threadsafe)
 * 
 * @author Maxwell
 */
class TickTimer<O extends GameEvent> extends TimerTask{

	/** the ServerNetwork to command*/
	protected ServerGameplay<O> g;
	
	public TickTimer(ServerGameplay<O> g){
		this.g = g;
	}
	
	@Override
	/*
	 * accepts clients until there are no more, or the server cannot take any more clients.
	 */
	public void run() {
		if(!g.tickingFlag){
			g.tickingFlag=true;
			g.tickUniverse();
			g.tickingFlag=false;
		}
	}
}