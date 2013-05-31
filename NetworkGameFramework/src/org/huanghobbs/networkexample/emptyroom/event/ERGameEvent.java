package org.huanghobbs.networkexample.emptyroom.event;

import org.huanghobbs.networkexample.emptyroom.GameObject;
import org.huanghobbs.networkframe.GameEvent;

public class ERGameEvent extends GameEvent{
	
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
	
	public Object[] data; //varies depending on what type of event it is
	
	protected ERGameEvent(long time, int type, Object[] data){
		super(time);
		this.eventType=type;
		this.data=data;
	}
	
	/**
	 * creates a new ERGameEvent to hold message data.
	 * 
	 * @param source
	 * @param message
	 * @return
	 */
	public static ERGameEvent EventMessage(long time, String source, String message){
		return new ERGameEvent( time, EVENT_MESSAGE, new Object[]{source,message});
	}
	
	public static ERGameEvent EventMessageClient(long time, String message){
		return new ERGameEvent( time, EVENT_MESSAGE_C, new Object[]{message});
	}
	
	public static ERGameEvent EventUpdateClient(long time, float destx, float desty){
		return new ERGameEvent( time, EVENT_UPDATE_C, new Object[]{destx, desty});
	}
	
	public static ERGameEvent EventUpdate(long time, int subjectID, float x, float y, float destx, float desty){
		return new ERGameEvent( time, EVENT_UPDATE, new Object[]{subjectID, x, y, destx, desty});
	}
	
	public static ERGameEvent EventUpdate(long time, GameObject o){
		return new  ERGameEvent( time, EVENT_UPDATE, new Object[]{o.id, o.x, o.y, o.destx, o.desty});
	}
	
	public static ERGameEvent EventDestroy(long time, int subjectID){
		return new ERGameEvent( time, EVENT_DESTROY, new Object[]{subjectID});
	}
	
	public static ERGameEvent EventInteractClient(long time, int subjectID){
		return new ERGameEvent( time, EVENT_INTERACT_C, new Object[]{subjectID});
	}
	
	public static ERGameEvent EventInteract(long time, int sourceID, int subjectID){
		return new ERGameEvent( time, EVENT_INTERACT, new Object[]{sourceID, subjectID});
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
