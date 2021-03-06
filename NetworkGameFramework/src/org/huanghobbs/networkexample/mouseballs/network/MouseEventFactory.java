package org.huanghobbs.networkexample.mouseballs.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.huanghobbs.networkframe.GameEvent;
import org.huanghobbs.networkframe.GameEventFactory;


public class MouseEventFactory extends GameEventFactory{

	@Override
	/**
	 * reads an event from inputstream
	 * 
	 * sent in this order:
	 * id
	 * x	y
	 * xvel yvel
	 * xacc	yacc
	 */
	protected GameEvent readEventStream(InputStream inputStream) throws IOException{
		MouseEvent newEvt = new MouseEvent();
		ObjectInputStream o = new ObjectInputStream(inputStream);
		if(!o.readBoolean()){
			newEvt.isPositionEvent=false;
			newEvt.identifier = o.readInt();
			newEvt.x = o.readFloat();
			newEvt.y = o.readFloat();
			newEvt.xvel = o.readFloat();
			newEvt.yvel = o.readFloat();
			newEvt.xdest = o.readFloat();
			newEvt.ydest = o.readFloat();
		} else{
			newEvt.isPositionEvent=true;
			newEvt.identifier = o.readInt();
			newEvt.x = o.readFloat();
			newEvt.y = o.readFloat();
		}
		
		return newEvt;
	}

	@Override
	protected void writeEventStream(GameEvent g, OutputStream outputStream) throws IOException {
		MouseEvent e = (MouseEvent)(g);
		ObjectOutputStream o = new ObjectOutputStream(outputStream);
		o.writeBoolean(e.isPositionEvent);
		o.writeInt(e.identifier);
		if(!e.isPositionEvent){
			o.writeFloat(e.x);
			o.writeFloat(e.y);
			o.writeFloat(e.xvel);
			o.writeFloat(e.yvel);
			o.writeFloat(e.xdest);
			o.writeFloat(e.xdest);
		} else{
			o.writeFloat(e.x);
			o.writeFloat(e.y);
		}
		o.flush();
	}

}
