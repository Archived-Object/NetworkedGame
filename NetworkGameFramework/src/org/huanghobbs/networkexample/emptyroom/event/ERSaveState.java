package org.huanghobbs.networkexample.emptyroom.event;

import java.util.HashMap;

import org.huanghobbs.networkexample.emptyroom.GameObject;
import org.huanghobbs.networkexample.emptyroom.gamelogic.EmptyRoomServer;
import org.huanghobbs.networkexample.emptyroom.gamelogic.EmptyRoomSimulation;
import org.huanghobbs.networkframe.SaveState;

public class ERSaveState extends SaveState{

	HashMap<Integer,float[]> record;
	
	public ERSaveState(EmptyRoomServer target) {
		super(target);
		record=makeRecord(target.objects);
	}
	public ERSaveState(EmptyRoomSimulation target){
		super(target);
		record=makeRecord(target.objects);
	}
	
	protected static HashMap<Integer,float[]> makeRecord(HashMap<Integer,GameObject> objects){
		HashMap<Integer,float[]> rec = new HashMap<Integer,float[]>();
		for(Integer i:objects.keySet()){
			GameObject o = objects.get(i);
			rec.put(i, new float[] {o.x, o.y, o.destx, o.desty} );
		}
		return rec;
		
	}
	@Override
	public void restoreToServer() {
		EmptyRoomServer game = (EmptyRoomServer)this.targetGame;
		for(Integer i:game.objects.keySet()){
			if(record.containsKey(i)){
				game.objects.get(i).x=record.get(i)[0];
				game.objects.get(i).y=record.get(i)[1];
				game.objects.get(i).destx=record.get(i)[2];
				game.objects.get(i).desty=record.get(i)[3];
				game.objects.get(i).sx=record.get(i)[0];
				game.objects.get(i).sy=record.get(i)[1];
			}else{
				game.objects.remove(i);
			}
		}
	}
	
	@Override
	/**
	 * lots of duplicate with restoreToServer because lolbadatprogramming
	 */
	public void restoreToClient() {
		EmptyRoomSimulation game = (EmptyRoomSimulation)this.targetGame;
		for(Integer i:game.objects.keySet()){
			if(record.containsKey(i)){
				game.objects.get(i).x=record.get(i)[0];
				game.objects.get(i).y=record.get(i)[1];
				game.objects.get(i).destx=record.get(i)[2];
				game.objects.get(i).desty=record.get(i)[3];
				game.objects.get(i).sx=record.get(i)[0];
				game.objects.get(i).sy=record.get(i)[1];
			}else{
				game.objects.remove(i);
			}
		}
	}

}
