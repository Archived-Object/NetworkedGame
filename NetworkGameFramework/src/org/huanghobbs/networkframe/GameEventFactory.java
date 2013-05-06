package org.huanghobbs.networkframe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * controls GameEvent interactions due to java's lack of true class methods. (cannot abstract & static)
 * only one instance may exist at a time.
 * 
 * @author Maxwell
 *
 */
public abstract class GameEventFactory {

	/** the (one) instance of GameEventFactory allowed at any time*/
	public static GameEventFactory instance;
	
	public GameEventFactory(){
		GameEventFactory.instance = this;
	}
	
	/**
	 * this method is called by the internal shit in ClientNetwork and ServerNetwork.
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static GameEvent readFromStream(InputStream inputStream) throws IOException{
		return GameEventFactory.instance.readEventStream(inputStream);
	}

	/**
	 * this method is called by the internal shit in ClientNetwork and ServerNetwork.
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static void writeToStream(GameEvent g, OutputStream outputStream) throws IOException {
		GameEventFactory.instance.writeEventStream(g,outputStream);
	}
	
	/**
	 * this (abstract) method reads a GameEvent from a specific inputStream.
	 * 
	 * @param inputStream the stream to read from
	 * @return a GameEvent
	 * @throws IOException if sockets or something happens
	 */
	protected abstract GameEvent readEventStream(InputStream inputStream) throws IOException;
	
	/**
	 * this (abstract) method writes a GameEvent from a specific inputStream.
	 * 
	 * @param g the event to write
	 * @param outputStream the stream to write to
	 * @return a GameEvent
	 * @throws IOException if sockets or something happens
	 */
	protected abstract void writeEventStream(GameEvent g, OutputStream outputStream) throws IOException;
	
}
