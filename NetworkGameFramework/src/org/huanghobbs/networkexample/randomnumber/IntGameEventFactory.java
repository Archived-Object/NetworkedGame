package org.huanghobbs.networkexample.randomnumber;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.huanghobbs.networkframe.GameEvent;
import org.huanghobbs.networkframe.GameEventFactory;


public class IntGameEventFactory extends GameEventFactory{

	@Override
	protected GameEvent readEventStream(InputStream inputStream) throws IOException{
		return new IntGameEvent(inputStream.read());
	}

	@Override
	protected void writeEventStream(GameEvent g, OutputStream outputStream) throws IOException {
		IntGameEvent e = (IntGameEvent)(g);
		outputStream.write(e.value);
	}

}
