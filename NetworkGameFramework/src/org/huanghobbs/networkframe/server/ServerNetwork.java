package org.huanghobbs.networkframe.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	 * gets a client by their ID
	 * @param id
	 * @return
	 */
	protected WrappedClient<G> getClientByID(int id){
		for(WrappedClient<G> w:this.clients){
			if(w.identifier == id){ return w; }
		}
		return null;
	}
	
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
				Socket newConnection = serverSocket.accept();
				
				ObjectOutputStream oos = new ObjectOutputStream(newConnection.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(newConnection.getInputStream());
				
				if(!ois.readBoolean()){//if it is a new person connecting
					
					//reading hashed finger, rehash and store.
					MessageDigest m = MessageDigest.getInstance("MD5");
					
					int length = ois.readInt();
					byte[] finger = new byte[length];
					ois.read(finger,0,length);
					
					WrappedClient<G> w = new WrappedClient<G>(newConnection, new String(m.digest(finger)) );
					
					//tell user their client number
					oos.writeInt( w.identifier );//send the client their value.
					oos.flush();
					
					synchronized(this.clients){
						this.clients.add( w );
					}
					System.out.println("accepting new connection, "+w.identifier);
					numClients++;
					new ClientReader<G>(this, w).start();	 
					this.serverGameplay.onConnect(w);
				}
				else{ //otherwise if reconnect
					WrappedClient<G> target = this.getClientByID(ois.readInt());
					
					int length = ois.readInt();
					byte[] finger = new byte[length];
					ois.read(finger,0,length);
					
					MessageDigest m = MessageDigest.getInstance("MD5");
					
					if( target.disconnected && new String(m.digest(finger)).equals(target.fingerprint)){//if the hashes match the fingerprint hash
						target.updateSocket(newConnection);
						new ClientReader<G>(this, target).start();	 
						this.serverGameplay.onReconnect(target);
						System.out.println("accepting reconnection of "+target);
					}
					else if (target.disconnected){
						newConnection.close();
						System.out.println("rejecting reconnection of "+target+", invalid confirmation");
						System.out.println(new String(m.digest(finger))+" "+new String(target.fingerprint));
					} else{
						newConnection.close();
						System.out.println("rejecting reconnection of "+target+", someone is already connected under that client");
					}
					
				}
				
				
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
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
		//checking if an event is illegal
		//kick client if they commit too many illegal actions
		if(!this.serverGameplay.handleEventWrapped(e,c)){
			c.illegalActions++;
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
