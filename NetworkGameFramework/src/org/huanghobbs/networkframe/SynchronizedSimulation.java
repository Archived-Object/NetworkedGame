package org.huanghobbs.networkframe;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class SynchronizedSimulation {
	
	private HashMap<String,Lock> checkedOutVariables = new HashMap<String,Lock>(0);
	
	/**
	 * 
	 * @param variableName
	 * @return true if the variable exists, false if it does not
	 */
	public boolean checkOutVariable(String variableName){
		
		if(this.checkedOutVariables.containsKey(variableName)){//wait until the field is not being blocked
			this.checkedOutVariables.get(variableName).lock();
		} else{
			this.checkedOutVariables.put(variableName,new ReentrantLock());
			this.checkedOutVariables.get(variableName).lock();
		}
		
		try {
			this.getClass().getDeclaredField(variableName);
			return true;
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			return false;//return false indicating there is no such field
		}
		return false;
	}
	
	public void releaseVariable(String variableName){
		this.checkedOutVariables.get(variableName).unlock();
	}
	
}
