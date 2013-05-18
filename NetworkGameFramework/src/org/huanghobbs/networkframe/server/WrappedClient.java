package org.huanghobbs.networkframe.server;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import org.huanghobbs.networkframe.GameEvent;
import org.huanghobbs.networkframe.GameEventFactory;


/**
 * class that encapsulates a socket to control a client.
 * meant to make handling disconnects easier
 * 
 * TODO any functionality whatsoever
 * 
 * @author Maxwell
 */
public class WrappedClient<G extends GameEvent> {
	
	Socket toClient = null;
	
	static int counter = 0;
	
	public int identifier;
	public boolean disconnected = false;
	public int illegalActions = 0; //tracker for how many illegal actions the client has committed
	
	String fingerprint;
	
	public WrappedClient(Socket s, String hashedFingerprint) throws SocketException{
		this.toClient=s;
		this.toClient.setSoTimeout(0);/** blocking */
		this.fingerprint=hashedFingerprint;
		this.identifier=counter;
		counter++;
	}
	
	/**
	 * gets a game event from the client. 
	 * non-blocking
	 * @return the game event (returns null if there is none to be gotten)
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public synchronized G getEvent(){
		try{
			G g = (G) GameEventFactory.readFromStream(this.toClient.getInputStream());
			return g;
		} catch(IOException e){
			System.err.println("cannot read event from client");
			e.printStackTrace();
			return null;
		}
	}
	

	/**
	 * sends an event to a client
	 * @param event the event to send
	 * 
	 * @return true if the client has disconnected
	 */
	public boolean sendEvent(G event) {
		try{
			GameEventFactory.writeToStream(event,this.toClient.getOutputStream());
			this.toClient.getOutputStream().flush();
		} catch(IOException e){
			System.out.println("cannot write event to outputstram");
			
			try {
				this.toClient.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public void updateSocket(Socket newSocket) {
		this.toClient=newSocket;
	}

}
