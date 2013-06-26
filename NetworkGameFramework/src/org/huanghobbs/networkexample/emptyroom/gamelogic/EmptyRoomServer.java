package org.huanghobbs.networkexample.emptyroom.gamelogic;

import java.util.HashMap;
import java.util.Random;

import org.huanghobbs.networkexample.emptyroom.GameObject;
import org.huanghobbs.networkexample.emptyroom.event.ERGameEvent;
import org.huanghobbs.networkframe.server.ServerGameplay;
import org.huanghobbs.networkframe.server.WrappedClient;

public class EmptyRoomServer extends ServerGameplay<ERGameEvent>{
	
	protected static final int interactRange = 50;

	protected static final int roomWidth = 750;
	protected static final int roomHeight = 550;
	
	protected Random random = new Random();
	
	public HashMap<Integer,GameObject> objects = new HashMap<Integer,GameObject>();
	
	
	public EmptyRoomServer(){
		super();
		this.tickTime = 500;
		this.maxRollback = 2000;
		
	}
	
	@Override
	public boolean handleEvent(ERGameEvent e, WrappedClient<ERGameEvent> source) {
		switch(e.eventType){
			case (ERGameEvent.EVENT_MESSAGE_C):
				String msg = (String) e.data[0];
			
				//if a command, parse it
				if(msg.charAt(0)=='\\'){
					parseChatCommand(msg);
				}
				//else do not
				else{
					network.dispatchEvent(ERGameEvent.EventMessage( this.currentTime(), objects.get(source.identifier).name , msg ));
				}
				return true;
				
			case (ERGameEvent.EVENT_UPDATE_C):
				float newx = (Float)(e.data[0]), //new destination x
					newy = (Float)(e.data[1]);//new destination y
				
				//limit motion requests to bounds of stage.
				if(newx<0){
					newx=0;
				} else if(newx>roomWidth){
					newx=roomWidth;
				}
				
				if(newy<0){
					newy=0;
				} else if(newy>roomHeight){
					newy=roomHeight;
				}
				
				this.objects.get(source.identifier).destx=newx;
				this.objects.get(source.identifier).desty=newy;
				network.dispatchEvent( ERGameEvent.EventUpdate( this.currentTime(), this.objects.get(source.identifier) ) );
				return true;
				
			case (ERGameEvent.EVENT_INTERACT_C):
				int targetID = (Integer) e.data[0];
				//if not in range, walk to object
				if( objects.get(source.identifier).getDistance( objects.get(targetID) ) > interactRange){
					objects.get(source.identifier).destx= objects.get(targetID).x;
					objects.get(source.identifier).destx= objects.get(targetID).y;
				}
				//otherwise, allow interaction to happen
				else{
					network.dispatchEvent(ERGameEvent.EventInteract( this.currentTime(), source.identifier, targetID));
				}
				return true;
			
			default:
				return false;
		}
	}
	
	/**
	 * steps forward the game state.
	 * 
	 * elapsed milliseconds are stored in this.elapsed. (compensate for lag)
	 */
	@Override
	public void tickUniverse(long elapsed){
		for(GameObject o:this.objects.values()){
			o.update(elapsed);
			network.dispatchEvent( ERGameEvent.EventUpdate(this.currentTime(), o) );
		}
	}
	
	/**
	 * does whatever chat command is done
	 * @param message
	 * @return
	 */
	protected void parseChatCommand(String message){
		
	}
	
	@Override
	/**
	 * spawns a new game object on new client connect
	 */
	public void onConnect(WrappedClient<ERGameEvent> justConnected) {
		this.objects.put(justConnected.identifier,
				new GameObject(
						justConnected.identifier,
						random.nextInt(roomWidth),
						random.nextInt(roomHeight),
						true
				)
			);
		network.dispatchEvent(
			ERGameEvent.EventUpdate(
				this.currentTime(),
				this.objects.get(justConnected.identifier )
			)
		);
	}

	@Override
	public void onDisconnect(WrappedClient<ERGameEvent> justDropped) {
		
	}

	@Override
	public void onReconnect(WrappedClient<ERGameEvent> justConnected) {
		
	}

}
