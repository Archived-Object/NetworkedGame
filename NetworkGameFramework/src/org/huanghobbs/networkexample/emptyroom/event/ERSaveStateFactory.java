package org.huanghobbs.networkexample.emptyroom.event;

import org.huanghobbs.networkexample.emptyroom.gamelogic.EmptyRoomServer;
import org.huanghobbs.networkexample.emptyroom.gamelogic.EmptyRoomSimulation;
import org.huanghobbs.networkframe.SaveState;
import org.huanghobbs.networkframe.SaveStateFactory;
import org.huanghobbs.networkframe.client.ClientSimulation;
import org.huanghobbs.networkframe.server.ServerGameplay;

public class ERSaveStateFactory extends SaveStateFactory<ERGameEvent>{

	@Override
	public SaveState makeServerSaveState(ServerGameplay<ERGameEvent> s) {
		return new ERSaveState((EmptyRoomServer)s);
	}

	@Override
	public SaveState makeClientSaveState(ClientSimulation<ERGameEvent> x) {
		return new ERSaveState((EmptyRoomSimulation)x);
	}

}
