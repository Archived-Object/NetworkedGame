package org.huanghobbs.networkframe.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

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
	
	protected boolean hasConnected=false;
	
	public int identifier;
	protected boolean disconnected = true;
	protected String fingerprint;
	
	public ClientNetwork(String address){
		this.networkThread = new NetworkThread<G>(this);
		this.address = address;
		
		this.fingerprint = getRandomString(16);//generates a unique fingerprint for the network part of this client.
	}
	
	public boolean isConnected(){
		return !disconnected;
	}
	
	public void setFingerprint(String fingerprint){
		this.fingerprint=fingerprint;
	}
	
	/**
	 * gets a random string of a given length
	 * 
	 * @param length the length of the string
	 * @return
	 */
	protected String getRandomString(int length){
		StringBuilder s = new StringBuilder();
		Random r = new Random();
		for(int i=0; i<length; i++){
			s.append( (char) (Character.MIN_CODE_POINT+r.nextInt(Character.MAX_CODE_POINT-Character.MIN_CODE_POINT) ) );
		}
		return s.toString();
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
			this.simulation.onDisconnect();
			if(!this.disconnected){
				this.disconnected=true;
			}
			return null;
		}
	}
	
	public void sendGameEvent(G evt){
		try {
			GameEventFactory.writeToStream(evt,this.socket.getOutputStream());
		} catch (Exception e) {
				System.err.println("ClientNetwork could not send GameEvent "+evt);
				e.printStackTrace();
			}
	}
	
	public void start() throws IOException{
		this.socket = new Socket();
		this.socket.connect( new InetSocketAddress(address, port) );
		this.socket.setSoTimeout(0);
		
		ObjectOutputStream oos = new ObjectOutputStream(this.socket.getOutputStream());
		ObjectInputStream ois = new ObjectInputStream(this.socket.getInputStream());
		
		//say if you have connected previously this session
		oos.writeBoolean(this.hasConnected);
		oos.flush();
		
		//on reconnect
		if(this.hasConnected){
			//say "I am this person, reconnecting"
			oos.writeInt(this.identifier);
			oos.flush();
		}
		
		//send hashed fingerprint to server
		MessageDigest m=null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] hashedFinger = m.digest(this.fingerprint.getBytes());
		oos.writeInt(hashedFinger.length);
		oos.write(hashedFinger);
		oos.flush();
		
		//on initial connect(){
		if(!this.hasConnected){
			this.identifier = ois.readInt();//get unique ID
			System.out.println("client has id of "+this.identifier);
			this.hasConnected=true;
		}
		this.disconnected=false;
		new NetworkThread<G>(this).start();
		
	}
	
	/**
	 * stops the network (disconnects from server)
	 */
	public void stop() {
		try {
			this.socket.close();
			this.disconnected=true;
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			while(shouldRun && !client.socket.isClosed()){
				O e = client.readGameEvent();
				if(e==null){
					this.cleanStop();
				} else{
					client.simulation.handleEventWrapped(e);
				}
			}
		}
		
		public void cleanStop(){
			this.shouldRun = false;
		}
	}
	
}
