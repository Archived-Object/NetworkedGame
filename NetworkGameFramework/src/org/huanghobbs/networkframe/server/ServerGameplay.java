package org.huanghobbs.networkframe.server;

import java.util.Timer;
import java.util.TimerTask;

import org.huanghobbs.networkframe.GameEvent;


/**
 * controls gameplay (server side) to dispatch gameplay events
 * also checks the validity of player-controlled gameplay events
 * 
 * @author Maxwell
 *
 */
public abstract class ServerGameplay<G extends GameEvent>{
	
	/** Static variable to control game universe "tick" speed*/
	protected static int universeTickTime = 30;
	
	/*things that are for this class only*/
	protected ServerNetwork<G> network;
	protected Timer gameTimer;
	protected boolean tickingFlag = false;
	
	/** convenience variable, elapsed milliseconds since last tick*/
	protected int elapsed = 100;//failsafe, in case someone doesn't call super.tickUniverse before using this.elapsed.
	protected long lastTick;
	
	/* communicate with outside this class*/
	public boolean initialized = false;

	/**
	 * creates a new ServerGameplay and ServerNetwork, then associates the two.
	 */
	public ServerGameplay(){
		this.network = new ServerNetwork<G>();
		this.network.setGame(this);
	}
	
	public void start(){
		if(!this.network.started){
			this.network.start();
		}
		this.initialized=true;

		this.lastTick=System.currentTimeMillis();
		
		this.gameTimer = new Timer();
		this.gameTimer.scheduleAtFixedRate(new TickTimer<G>(this), 0, universeTickTime);
	}
	
	
	//GAMEPLAY content
	
	/**
	 * check if an incoming event is valid (allowed by game)
	 * if it is valid, the event is processed (applied to game and redispatched)
	 * 
	 * @param e the event that is to be checked/implemented
	 * @return if the event was valid
	 */
	public abstract boolean handleEvent(G e, WrappedClient<G> source);
	
	/**
	 * advances the universe of the game.
	 * should be called at the beginning of all subclasses' tickUniverse methods
	 * (does the elapsed calculations)
	 */
	public void tickUniverse(){
		long p =System.currentTimeMillis();
		this.elapsed = (int)(p-this.lastTick);
		this.lastTick = p;
	}

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