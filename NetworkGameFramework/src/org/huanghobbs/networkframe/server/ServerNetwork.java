package org.huanghobbs.networkframe.server;

import java.awt.Event;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.huanghobbs.networkframe.GameEvent;


public class ServerNetwork<G extends GameEvent>{
	
	/**Static behavior variables*/
	protected static final int port = 5244;
	protected static final int maxclients = 5;
	protected static final int upkeepTimer = 1000;
	protected static final int eventTimer = 100;

	protected boolean kickDisconnected = false;
	
	/**things that change*/
	protected ServerSocket serverSocket;
	protected ServerGameplay<G> serverGameplay = null;
	protected Timer networkTimer;
	protected ArrayList<WrappedClient<G>> clients = new ArrayList<WrappedClient<G>>(0);
	
	public boolean eventPipingFlag = false;
	
	/* communicate with outside this class*/
	public boolean started = false;

	/**
	 * removes disconnected clients and checks for new connections
	 */
	public void checkConnections(){
		while(this.getNewClient()){} //gets all new clients until full or no new clients
		
		if(kickDisconnected){
			for(int i=0; i<this.clients.size(); i++){
				if(!this.clients.get(i).isConnected()){
					this.clients.remove(i);
				}
			}
		}
	}
	
	/**
	 * checks to see if there is an available client, and if so, adds them.
	 * also sets up a thread to make blocking calls to client.getEvent().
	 * 
	 * @return if the client was available and accepted
	 */
	public boolean getNewClient(){
		if(this.clients.size()<maxclients){
			try {
				
				WrappedClient<G> w = new WrappedClient<G>(serverSocket.accept());
				w.informClient();
	            this.clients.add( w );
	            new ClientReader<G>(this, w).start();	            
	            this.serverGameplay.onConnect(w);
	            
	        } catch (IOException e) {
	        	return false;
	        }
		} else{
			return false;
		}
		return true;
	}
	
	


	public void setGame(ServerGameplay<G> game){
		this.serverGameplay = game;
	}
	
	/**
	 * sends an event to all currently connected clients.
	 * meant to be called by ServerGame, so there is no parody checking for valid events.
	 * 
	 * @param event the game event you want to send
	 */
	public void dispatchEvent(G event){
		for(int i=0; i<this.clients.size(); i++){
			if(this.clients.get(i).isConnected()){
				if(this.clients.get(i).sendEvent(event)){//try to send event, on disconnect, execute this code
					this.serverGameplay.onDisconnect(this.clients.get(i));
					if(this.kickDisconnected){
						this.clients.remove(i);
						i--;
					}
				}
			}
		}
	}
	
	/**
	 * sends an event to all currently connected clients.
	 * meant to be called by ServerGame, so there is no parody checking for valid events.
	 * 
	 * @param event the game event you want to send
	 * @throws IOException 
	 */
	public void handleEvent(G e, WrappedClient<G> c) throws IOException{
		if(this.serverGameplay.handleEvent(e,c)){
			this.dispatchEvent(e);
		}
	}
	
	
	
	/**
	 * the setup function of the networked server.
	 * it sets up all the timers and listeners required.
	 */
	public void start(){
		
		try { //setting up client connections
			this.serverSocket= new ServerSocket(port);
		} catch (IOException e1) {
            System.err.println("Could not listen on port "+port);
            e1.printStackTrace();
            System.exit(-1);
		}
		
		this.networkTimer = new Timer();
		this.networkTimer.scheduleAtFixedRate(new NetworkMaintenenceTimer<G>(this), 0, upkeepTimer);	
		
		this.started=true;
	}
	
}




/**
 * timer task that tells the ServerNetwork to manage connections
 * this means accepting new connections, removing disconnected clients, etc.
 * 
 * @author Maxwell
 */
class NetworkMaintenenceTimer<O extends GameEvent> extends TimerTask{

	/** the ServerNetwork to command*/
	protected ServerNetwork<O> s;
	
	public NetworkMaintenenceTimer(ServerNetwork<O> s){
		this.s = s;
	}
	
	@Override
	/*
	 * accepts clients until there are no more, or the server cannot take any more clients.
	 */
	public void run() {
		s.checkConnections();
	}
}

/**
 * timer task that tells the ServerNetwork to do event management 
 * @author Maxwell
 */
class ClientReader <F extends GameEvent> extends Thread{

	/** the ServerNetwork to command*/
	protected ServerNetwork<F> s;
	protected WrappedClient<F> c;
	
	public ClientReader(ServerNetwork<F> s, WrappedClient<F> c){
		super();
		this.s = s;
		this.c = c;
	}
	
	@Override
	/*
	 * constantly makes blocking calls to client.getEvent, and tells the server to handle that event
	 */
	public void run() {
		while(true){
			try {
				F e = this.c.getEvent();
				this.s.handleEvent(e, c);
			} catch (IOException e) {
				System.err.println("could not make call to client, dropping them because fuck you");
				e.printStackTrace();
				break;
			}
		}
	}
}
