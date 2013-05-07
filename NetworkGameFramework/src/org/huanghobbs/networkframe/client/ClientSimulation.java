package org.huanghobbs.networkframe.client;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.huanghobbs.networkframe.GameEvent;
import org.huanghobbs.networkframe.SynchronizedSimulation;
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
public abstract class ClientSimulation<G extends GameEvent> extends SynchronizedSimulation{

	/** Static variable to control game simulation "tick" speed*/
	protected static int simulationTickTime = 30;
	
	/** The networking half that the simulated client recieves data from */
	public ClientNetwork<G> network;
	
	/** convenience variables, elapsed milliseconds since last tick*/
	protected int elapsed = 100;//failsafe, in case someone doesn't call super.tickUniverse before using this.elapsed.
	protected long lastTick;
	protected boolean manualTick = false;
	
	/** timer*/
	protected Timer simulationTimer = new Timer();
	
	public ClientSimulation(String targetAddress){
		this.network = new ClientNetwork<G>(targetAddress);
		this.network.setSimulation(this);
	}
	
	public void start(){
		this.lastTick=System.currentTimeMillis();
		if(!manualTick){
			this.simulationTimer.scheduleAtFixedRate(new SimulationTicker<G>(this), 0, simulationTickTime);
		}
		
		try{
			this.network.start();
		} catch(IOException e){
			System.err.println("cannot start client network half.");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void tickSimulation(){
		long p =System.currentTimeMillis();
		this.elapsed = (int)(p-this.lastTick);
		this.lastTick = p;
	}
	
	/**
	 * abstract method
	 * handle a GameEvent (update the simulation and apply dead-reckoning to figure out what should be going on)
	 */
	public abstract void handleEvent(G e);
	
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