package org.huanghobbs.networkframe;

import org.huanghobbs.networkframe.client.ClientSimulation;
import org.huanghobbs.networkframe.server.ServerGameplay;

/**
 * controls SaveState Generation
 * is ugly in order to allow for Save-state generation in untyped/generic
 * forms of the classes (in the abstract networking library)
 * 
 * @author Maxwell
 *
 */
public abstract class SaveStateFactory<G extends GameEvent> {

	public long minRollbackTime=5000;
	
	/** the (one) instance of GameEventFactory allowed at any time*/
	@SuppressWarnings("rawtypes")
	public static SaveStateFactory instance;
	
	public SaveStateFactory(){
		SaveStateFactory.instance = this;
	}
	

	@SuppressWarnings("unchecked")
	public static SaveState makeClientState(ClientSimulation<?> g){
		return SaveStateFactory.instance.makeClientSaveState(g);
	}

	@SuppressWarnings("unchecked")
	public static SaveState makeServerState(ServerGameplay<?> g){
		return SaveStateFactory.instance.makeServerSaveState(g);
	}
	
	public abstract SaveState makeServerSaveState(ServerGameplay<G> s);
	public abstract SaveState makeClientSaveState(ClientSimulation<G> x);
}
