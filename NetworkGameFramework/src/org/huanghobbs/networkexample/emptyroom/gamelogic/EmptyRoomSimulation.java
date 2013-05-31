package org.huanghobbs.networkexample.emptyroom.gamelogic;

import java.util.HashMap;

import org.huanghobbs.networkexample.emptyroom.EmptyRoom;
import org.huanghobbs.networkexample.emptyroom.GameObject;
import org.huanghobbs.networkexample.emptyroom.event.ERGameEvent;
import org.huanghobbs.networkframe.client.ClientSimulation;

public class EmptyRoomSimulation extends ClientSimulation<ERGameEvent>{	

	public HashMap<Integer, GameObject> objects =
		new HashMap<Integer, GameObject>();
	
	public EmptyRoomSimulation(String targetAddress) {
		super(targetAddress);
		this.manualTick=true;
	}

	@Override
	public void handleEvent(ERGameEvent e) {
		switch(e.eventType){
		case ERGameEvent.EVENT_MESSAGE:
			EmptyRoom.logMessage(e.data[0]+": "+e.data[0].toString());
			break;
		case ERGameEvent.EVENT_UPDATE:
			if(this.objects.containsKey( (e.data[0])) ){
				objects.get( (e.data[0]) ).updateFromServer(e);  
			} else{
				objects.put( (Integer)(e.data[0]) , GameObject.makeFromEvent(e)); 
			}
			break;
		case ERGameEvent.EVENT_DESTROY:
			objects.remove( (e.data[0]) );
			break;
		case ERGameEvent.EVENT_INTERACT:
			//TODO event interactions;s
			break;
		default:
			System.err.println("can't parse event "+e.toString());
			break;
		}
	}
	
	@Override
	public void tickSimulation(long elapsed){
		for( GameObject o: objects.values()){
			o.update(elapsed);
		}
	}
	
	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReconnect() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void start(){
		super.start();
		this.startNetwork();
	}

}
