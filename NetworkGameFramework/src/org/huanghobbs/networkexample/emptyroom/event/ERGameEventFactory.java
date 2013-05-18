package org.huanghobbs.networkexample.emptyroom.event;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.huanghobbs.networkframe.GameEvent;
import org.huanghobbs.networkframe.GameEventFactory;

/**
 * manages many self-contained sub-types.
 * maybe better to handle this with subclasses, buttfuck it.
 * 
 * @author Maxwell
 */
public class ERGameEventFactory extends GameEventFactory{
	
	@Override
	protected ERGameEvent readEventStream(InputStream inputStream)
			throws IOException {
		ObjectInputStream ois = new ObjectInputStream(inputStream);
		
		switch(ois.readInt()){
			case (ERGameEvent.EVENT_MESSAGE_C):
				return ERGameEvent.EventMessageClient(ois.readUTF());
			case (ERGameEvent.EVENT_MESSAGE):
				return ERGameEvent.EventMessage(
						ois.readUTF(),
						ois.readUTF());
			case (ERGameEvent.EVENT_UPDATE_C):
				return  ERGameEvent.EventUpdateClient(
						 ois.readFloat(),
						 ois.readFloat() );
			case (ERGameEvent.EVENT_UPDATE):
				return ERGameEvent.EventUpdate(
						ois.readInt(),
						ois.readFloat(), 
						ois.readFloat(),
						ois.readFloat(),
						ois.readFloat() );
			case (ERGameEvent.EVENT_DESTROY):
				return ERGameEvent.EventDestroy(
						ois.readInt() );
			case (ERGameEvent.EVENT_INTERACT_C):
				return ERGameEvent.EventInteractClient(
						ois.readInt() );
			case (ERGameEvent.EVENT_INTERACT):
				return ERGameEvent.EventInteract(
						ois.readInt(),
						ois.readInt() );
			default:
				return null;
		}
	}
	
	@Override
	protected void writeEventStream(GameEvent g, OutputStream outputStream)
			throws IOException {
		ERGameEvent e = (ERGameEvent)g;
		ObjectOutputStream oos = new ObjectOutputStream(outputStream);
		
		oos.writeInt(e.eventType);
		
		switch(e.eventType){
			case (ERGameEvent.EVENT_MESSAGE_C):
				oos.writeUTF((String)(e.data[0]));
				break;
			case (ERGameEvent.EVENT_MESSAGE):
				oos.writeUTF((String)(e.data[0]));
				oos.writeUTF((String)(e.data[1]));
				break;
			case (ERGameEvent.EVENT_UPDATE_C):
				oos.writeFloat((Float)(e.data[0]));//new destination x
				oos.writeFloat((Float)(e.data[1]));//new destination y
				break;
			case (ERGameEvent.EVENT_UPDATE):
				oos.writeInt((Integer)(e.data[0]));//id
				oos.writeFloat((Float)(e.data[1]));//x
				oos.writeFloat((Float)(e.data[2]));//y
				oos.writeFloat((Float)(e.data[3]));//destination x
				oos.writeFloat((Float)(e.data[4]));//destination y
				break;
			case (ERGameEvent.EVENT_DESTROY):
				oos.writeInt((Integer)(e.data[0]));//id
				break;
			case (ERGameEvent.EVENT_INTERACT_C):
				oos.writeInt((Integer)(e.data[0]));//id of target of interaction
				break;
			case (ERGameEvent.EVENT_INTERACT):
				oos.writeInt((Integer)(e.data[0]));//id of source of interaction
				oos.writeInt((Integer)(e.data[1]));//id of target of interaction
				break;
			default:
				break;
		}
		oos.flush();
	}

}
