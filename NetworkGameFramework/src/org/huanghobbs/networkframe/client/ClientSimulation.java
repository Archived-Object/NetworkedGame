package org.huanghobbs.networkframe.client;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.huanghobbs.networkframe.GameEvent;
import org.huanghobbs.networkframe.GameTimer;
import org.huanghobbs.networkframe.SaveState;
import org.huanghobbs.networkframe.SaveStateFactory;
/**
 * This handles dead reckoning. (simulating based on last update from the server)
 * This also handles recording and dispatching player actions as GameEvents
 * 
 * 
 * it should hold the main game loop.
 * 
 * @author Maxwell
 *
 */
public abstract class ClientSimulation<G extends GameEvent> extends GameTimer{

	/** Static variable to control game simulation "tick" speed*/
	protected int tickTime = 30;
	
	/** The networking half that the simulated client recieves data from */
	public ClientNetwork<G> network;
	
	/** convenience variables, elapsed milliseconds since last tick*/
	protected boolean manualTick = false;
	
	/** timer*/
	protected Timer simulationTimer = new Timer();
	protected LinkedList<G> eventRecord = new LinkedList<G>();
	SaveState lastBackup;
	
	public ClientSimulation(String targetAddress){
		this.network = new ClientNetwork<G>(targetAddress);
		this.network.setSimulation(this);
	}
	
	@Override
	public void start(){
		super.start();
		this.lastTick=0;
		this.lastBackup=SaveStateFactory.makeClientState(this);
		if(!manualTick){
			this.simulationTimer.scheduleAtFixedRate(new SimulationTicker<G>(this), 0, tickTime);
		}
		this.lastBackup=SaveStateFactory.makeClientState(this);
	}
	
	/**
	 * convenience, calls network.start with IOerror handling.
	 */
	public void startNetwork(){
		try{
			this.network.start();
		} catch(IOException e){
			System.err.println("cannot start client network half.");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void tickSimulation(){
		synchronized(this.eventRecord){//makes sure it doesn't overlap with handleEvent
			long p = this.currentTime();
			long elapsed = (int)(p-this.lastTick);
			this.lastTick = p;
			tickSimulation(elapsed);
			if(this.lastBackup.gameTime<=this.lastTick-this.maxRollback+this.tickTime){
				//creates a more recent save state
				this.lastBackup= SaveStateFactory.makeClientState(this);
				//clears out saved game events that are expired now
				for( G evt: this.eventRecord){
					if(evt.eventTime<this.lastBackup.gameTime){
						this.eventRecord.pollFirst();
					}
				}
			}
		}
	}
	
	/**
	 * abstract method
	 * 
	 * must set lastTick as well
	 * @param targetTime the time to roll back 
	 */
	public void rollbackTo(long targetGameTime){
		lastBackup.restore();
		tickForwardTo(targetGameTime);
	}
	
	/**
	 * ticks the game forward, setting lastTick at the end
	 * @param targetGameTime the time to tick to
	 */
	public void tickForwardTo(long targetGameTime){//TODO recorded events
		long diff = targetGameTime-lastTick;
		for( int i=0; i<diff/this.tickTime; i++){
			this.tickSimulation(this.tickTime);
		}
		if(diff%this.tickTime!=0){
			this.tickSimulation(diff%this.tickTime);
		}
		this.lastTick = targetGameTime;
	}

	public abstract void tickSimulation(long elapsedMillis);
	
	public void handleEventWrapped(G e){
		synchronized(this.eventRecord){
			if(e.eventTime<this.lastTick &&
					(this.lastTick-e.eventTime<maxRollback || maxRollback==-1) ){
				long currentTime = this.lastTick;//if you can, roll back and apply the event from the server, then roll forward again
				this.rollbackTo(e.eventTime);
				//TODO add it to the events queue
				this.tickForwardTo(currentTime);
			}
			else if (e.eventTime>this.lastTick){
				//always trust the server. (roll forward to match server time)
				this.tickForwardTo(e.eventTime);
				this.handleEvent(e);
			}
		}
	}
	
	/**
	 * abstract method
	 * handle a GameEvent (update the simulation and apply dead-reckoning to figure out what should be going on)
	 */
	protected abstract void handleEvent(G e);

	/**
	 * abstract method
	 * handles a disconnect from the parent server
	 */
	public abstract void onDisconnect();
	
	/**
	 * abstract method
	 * handles a reconnect to the parent server.
	 */
	public abstract void onReconnect();
	
	class SimulationTicker<O extends GameEvent> extends TimerTask{

		ClientSimulation<O> s;
		
		public SimulationTicker(ClientSimulation<O> s){
			this.s = s;
		}
		
		@Override
		public void run() {
			s.tickSimulation();
		}
		
	}

	
}