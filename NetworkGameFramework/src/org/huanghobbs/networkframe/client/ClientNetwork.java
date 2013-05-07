package org.huanghobbs.networkframe.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import org.huanghobbs.networkframe.GameEvent;
import org.huanghobbs.networkframe.GameEventFactory;


/**
 * 
 * This handles network output, so it is in charge of
 * handling inputs and sending the proper corresponding events to the server.
 * 
 * that's it.
 * 
 * @author Maxwell
 *
 */
public class ClientNetwork <G extends GameEvent>{
	
	/**Static behavior variables*/
	protected static final int port = 1337;
	
	/**the simulation that the gameEvents coming in/out of this govern*/
	protected ClientSimulation<G> simulation;
	
	/**networking shit*/
	protected NetworkThread<G> networkThread;
	protected Socket socket;
	protected String address;
	
	public int identifier;
	
	public ClientNetwork(String address){
		this.networkThread = new NetworkThread<G>(this);
		this.socket = new Socket();
		this.address = address;
		
		try {
			this.socket.setSoTimeout(0);
		} catch (SocketException e) {
			System.err.println("could bot set socket timeout to infinite (blocking)");
			e.printStackTrace();
		}
	}
	
	public void setSimulation(ClientSimulation<G> c){
		this.simulation = c;
	}
	
	/**
	 * gets a gameEvent from the socket (blocking)
	 * if there is an error in reading (i.e. the socket closes or something like that)
	 * it will return null.
	 * @return the GameEvent, null on error.
	 */
	@SuppressWarnings("unchecked")
	public G readGameEvent() {
		try {
			return (G) GameEventFactory.readFromStream(this.socket.getInputStream());
		} catch (IOException e) {
			System.err.println("ClientNetwork could not read GameEvent. What is going on?");
			e.printStackTrace();
			return null;
		}
	}
	
	public void sendGameEvent(G evt){
		try {
			GameEventFactory.writeToStream(evt,this.socket.getOutputStream());
		} catch (IOException e) {
				System.err.println("ClientNetwork could not send GameEvent");
				e.printStackTrace();
			}
	}
	
	public void start() throws IOException{
		this.socket.connect( new InetSocketAddress(address, port) );
		this.identifier = this.socket.getInputStream().read();
		this.networkThread.start();
	}
	
	/**
	 * a thread that tells the ClientNetwork to constantly make calls to
	 * client.readGameEvent(), which will block until a message is received.
	 * 
	 * @author Maxwell Huang-Hobbs
	 *
	 */
	protected class NetworkThread<O extends GameEvent> extends Thread{
		
		protected ClientNetwork<O> client;
		protected boolean shouldRun=true;
		
		public NetworkThread(ClientNetwork<O> client){
			this.client = client;
		}
		
		@Override
		public void run(){
			while(shouldRun){
				O e = client.readGameEvent();
				if(e==null){
					this.cleanStop();
					//TODO handling disconnects
				} else{
					client.simulation.handleEvent(e);
				}
			}
		}
		
		public void cleanStop(){
			this.shouldRun = false;
		}
	}
	
}
