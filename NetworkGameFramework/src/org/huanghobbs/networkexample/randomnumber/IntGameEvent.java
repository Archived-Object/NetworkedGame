package org.huanghobbs.networkexample.randomnumber;

import org.huanghobbs.networkframe.GameEvent;

public class IntGameEvent implements GameEvent{
	
	public int value;
	
	public IntGameEvent(){}
	
	public IntGameEvent(int value){
		this.value = value;
	}

}
