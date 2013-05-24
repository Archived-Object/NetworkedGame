package org.huanghobbs.networkexample.emptyroom.event;

import org.huanghobbs.networkexample.emptyroom.EmptyRoom;
import org.huanghobbs.networkexample.emptyroom.GameObject;
import org.huanghobbs.networkframe.GameEvent;

public class ERGameEvent implements GameEvent{
	
	//tag EVENT_*_C means this event is only sent from client to server, and the server figures out who it is.
	
	public static final int 
		EVENT_MESSAGE_C=0,	// message (string)
		EVENT_MESSAGE=1,	// source(string) message (string)
		EVENT_UPDATE_C=2,	// subject(int) x(float) y(float)
		EVENT_UPDATE=3,		// subject(int) x(float) y(float) x_destination (float) y_destination(float)
		EVENT_DESTROY=4,	// subject(int)
		EVENT_INTERACT_C=5,	// target(int)
		EVENT_INTERACT=6;	// subject(int) target(int)
	
	public int eventType;
	public long eventTime;//time of event, with respect to game time (milliseconds since beginning of game)
	
	public Object[] data; //varies depending on what type of event it is
	
	/**
	 * protected, holds arbitrary data as defined by the event type
	 * @param type the type of event it is
	 * @param data the list of arguments to contain
	 */
	protected ERGameEvent(int type, Object[] data){
		this(type, data, System.currentTimeMillis()-EmptyRoom.gameStart);
	}
	protected ERGameEvent(int type, Object[] data,long gameTime){
		this.eventType=type;
		this.data=data;
		this.eventTime=gameTime;
	}
	
	/**
	 * creates a new ERGameEvent to hold message data.
	 * 
	 * @param source
	 * @param message
	 * @return
	 */
	public static ERGameEvent EventMessage(String source, String message){
		return new ERGameEvent( EVENT_MESSAGE, new Object[]{source,message});
	}
	
	public static ERGameEvent EventMessageClient(String message){
		return new ERGameEvent( EVENT_MESSAGE_C, new Object[]{message});
	}
	
	public static ERGameEvent EventUpdateClient(float destx, float desty){
		return new ERGameEvent( EVENT_UPDATE_C, new Object[]{destx, desty});
	}
	
	public static ERGameEvent EventUpdate(int subjectID, float x, float y, float destx, float desty){
		return new ERGameEvent( EVENT_UPDATE, new Object[]{subjectID, x, y, destx, desty});
	}
	
	public static ERGameEvent EventUpdate(GameObject o){
		return new  ERGameEvent( EVENT_UPDATE, new Object[]{o.id, o.x, o.y, o.destx, o.desty});
	}
	
	public static ERGameEvent EventDestroy(int subjectID){
		return new ERGameEvent( EVENT_DESTROY, new Object[]{subjectID});
	}
	
	public static ERGameEvent EventInteractClient(int subjectID){
		return new ERGameEvent( EVENT_INTERACT_C, new Object[]{subjectID});
	}
	
	public static ERGameEvent EventInteract(int sourceID, int subjectID){
		return new ERGameEvent( EVENT_INTERACT, new Object[]{sourceID, subjectID});
	}
	
	protected String getTypeName(){
		switch(this.eventType){
			case(EVENT_MESSAGE_C):
				return "Message_Client";
			case(EVENT_MESSAGE):
				return "Message";
			case(EVENT_UPDATE_C):
				return "Update_Client";
			case(EVENT_UPDATE):
				return "Update";
			case(EVENT_DESTROY):
				return "Destroy";
			case(EVENT_INTERACT_C):
				return "Interact_Client";
			case(EVENT_INTERACT):
				return "Interact";
			default:
				return "UNKNOWN_TYPE";
		}
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder(getTypeName());
		sb.append(' ');
		for(int i=0; i<this.data.length; i++){
			sb.append(data[i].toString());
			if(i!=this.data.length-1){
				sb.append(" ");
			}
		}
		return sb.toString();
	}
	
}
