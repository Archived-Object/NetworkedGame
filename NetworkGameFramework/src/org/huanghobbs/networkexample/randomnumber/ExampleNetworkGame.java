package org.huanghobbs.networkexample.randomnumber;

import java.util.Random;

import org.huanghobbs.networkframe.client.ClientSimulation;
import org.huanghobbs.networkframe.server.ServerGameplay;
import org.huanghobbs.networkframe.server.WrappedClient;


/**
 * a simple example to echo random numbers from 0-100 and show how prediction would work by using the networkedGame setup.
 * 
 * 
 * @author Maxwell Huang-Hobbs
 *
 */
public class ExampleNetworkGame {

	ExampleServer s;
	ExampleClient c;
	
	public static void main(String[] args){
		new IntGameEventFactory();
		ExampleNetworkGame example = new ExampleNetworkGame();
		example.start();
	}
	
	public ExampleNetworkGame(){
		this.s = new ExampleServer();
		this.c = new ExampleClient();
	}
	
	public void start(){
		s.start();
		c.start();
	}

	/**
	 * the server half
	 * @author Maxwell
	 *
	 */
	protected class ExampleServer extends ServerGameplay<IntGameEvent>{

		int recentDigit;
		boolean advanced = true;
		
		@Override
		public boolean handleEvent(IntGameEvent e, WrappedClient<IntGameEvent> c) {
			this.recentDigit = e.value;
			this.advanced=false;
			network.dispatchEvent(e);//echo event to all clients
			return true;//treat all events as valid
		}
		
		@Override
		public void tickUniverse(){
			super.tickUniverse();
			if(!advanced){
				this.recentDigit+=1;
				this.network.dispatchEvent(new IntGameEvent(this.recentDigit));
				this.advanced=true;
			}
		}

		@Override
		public void onConnect(WrappedClient<IntGameEvent> justConnected) {
			//do nothing on connect
		}

		@Override
		public void onDisconnect(WrappedClient<IntGameEvent> justConnected) {
			//do nothing on disconnect
		}

		@Override
		public void onReconnect(WrappedClient<IntGameEvent> justConnected) {
			//do nothing on reconnect
		}
		
	}
	
	/**
	 * the client half
	 * @author Maxwell
	 *
	 */
	protected class ExampleClient extends ClientSimulation<IntGameEvent>{
		
		/** the time between sending messages to server */
		protected static final int calltime = 3000;
		
		int recentDigit = 0;
		int netElapsed = 2000;
		boolean advanced = true;
		
		Random generator = new Random();
		
		public ExampleClient(){
			super("localhost");
		}
		
		@Override
		public void tickSimulation(){
			super.tickSimulation();
			
			System.out.print(".");
			
			this.netElapsed+=this.elapsed;
			if(this.netElapsed>calltime){
				this.netElapsed = this.netElapsed-calltime;
				
				this.recentDigit = (int)(generator.nextDouble()*99);
				this.network.sendGameEvent( new IntGameEvent(recentDigit) );
				this.advanced=false;

				System.out.println("\n"+recentDigit+" sent by client");
			}
			
			if(!advanced){
				this.recentDigit+=1;
				System.out.println(recentDigit+" predicted by client");
				this.advanced=true;
			}
		}
		
		@Override
		public void start(){
			super.start();
			this.startNetwork();
		}
		
		@Override
		/**
		 * applies an event from the parent simulation
		 */
		public void handleEvent(IntGameEvent e) {
			System.out.println(e.value +" recieved from server");
		}

		/**
		 * does nothing
		 */
		@Override
		public void onDisconnect() {
			
		}

		/**
		 * does nothing
		 */
		@Override
		public void onReconnect() {
			
		}
		
	} 

}
