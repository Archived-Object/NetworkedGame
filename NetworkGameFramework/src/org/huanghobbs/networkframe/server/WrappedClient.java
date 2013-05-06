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
	
	public WrappedClient(Socket s) throws SocketException{
		this.toClient=s;
		this.toClient.setSoTimeout(0);/** blocking */
		this.identifier=counter;
		counter++;
	}
	
	public void informClient() throws IOException {
        this.toClient.getOutputStream().write(this.identifier);
        this.toClient.getOutputStream().flush();
	}
	
	/**
	 * gets a game event from the client. 
	 * non-blocking
	 * @return the game event (returns null if there is none to be gotten)
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public G getEvent() throws IOException{
		return (G) GameEventFactory.readFromStream(this.toClient.getInputStream());
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
		} catch(IOException e){
			System.out.println("cannot write event to outputstram");
			e.printStackTrace();
			
			try {
				this.toClient.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @return if the client is connected
	 */
	public boolean isConnected(){
		return this.toClient!=null && this.toClient.isConnected();
	}

}
