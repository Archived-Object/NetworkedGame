package org.huanghobbs.networkframe.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;

import org.huanghobbs.networkframe.GameEvent;


public class ServerNetwork<G extends GameEvent>{
	
	/**Static behavior variables*/
	protected static final int port = 1337;
	protected static final int maxclients = 5;
	protected static final int upkeepTimer = 1000;
	protected static final int eventTimer = 100;

	protected boolean kickDisconnected = false;
	protected int numClients = 0;
	
	/**things that change*/
	protected ServerSocket serverSocket;
	protected ServerGameplay<G> serverGameplay = null;
	protected ArrayList<WrappedClient<G>> clients = new ArrayList<WrappedClient<G>>(0);
	
	public boolean eventPipingFlag = false;
	
	/* communicate with outside this class*/
	public boolean started = false;

	/**
	 * checks to see if there is an available client, and if so, adds them.
	 * also sets up a thread to make blocking calls to client.getEvent().
	 * 
	 * this method will block until a client is found.
	 * 
	 * @return if the client was available and accepted
	 */
	public void getNewClient(){
		if(this.numClients < maxclients){
			try {
				WrappedClient<G> w = new WrappedClient<G>(serverSocket.accept());
				w.informClient();
				synchronized(this.clients){
					this.clients.add( w );
					new ClientReader<G>(this, w).start();	            
					this.serverGameplay.onConnect(w);
				}
				numClients++;
				
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
			if(!this.clients.get(i).disconnected){
				this.clients.get(i).sendEvent(event); //try to send event
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
	public void handleEvent(G e, WrappedClient<G> c){
		if(this.serverGameplay.handleEvent(e,c)){
			this.dispatchEvent(e);
		}
	}
	
	/**
	 * marks a client as disconnected, and tells the serverGameplay about it
	 * will remove them if that behavior is specified in static defining variables.
	 * @param c
	 */
	public void handleDisconnect(WrappedClient<G> c){
		c.disconnected=true;
		serverGameplay.onDisconnect(c);
		
		if(this.kickDisconnected){
			synchronized(this.clients){
				this.clients.remove(c);
				this.numClients--;
			}
		}
	}
	
	/**
	 * the setup function of the networked server.
	 * it sets up all the timers and listeners required.
	 */
	public void start(){
		
		try { //setting up client connections
			this.serverSocket= new ServerSocket(port);
			this.serverSocket.setSoTimeout(0);
			new ConnectionListener<G>(this).start();
			this.started=true;
			
		} catch (IOException e1) {
            System.err.println("Could not listen on port "+port);
            e1.printStackTrace();
            System.exit(-1);
		}
		
	}
	
}




/**
 * timer task that tells the ServerNetwork to manage connections
 * this means accepting new connections, removing disconnected clients, etc.
 * 
 * @author Maxwell
 */
class ConnectionListener<O extends GameEvent> extends Thread{

	/** the ServerNetwork to command*/
	protected ServerNetwork<O> s;
	
	public ConnectionListener(ServerNetwork<O> s){
		this.s = s;
	}
	
	@Override
	/*
	 * sends unending, blocking calls co accept new clients.
	 */
	public void run() {
		while(true){
			s.getNewClient();
		}
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

			F e = this.c.getEvent();
			if(e!=null){
				this.s.handleEvent(e, c);
			}
			else{
				System.err.println("could not make call to client, dropping their update loop because fuck you");
				s.handleDisconnect(c);
				break;
			}
		}
	}
}
