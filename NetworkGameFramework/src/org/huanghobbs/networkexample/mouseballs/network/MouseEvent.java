package org.huanghobbs.networkexample.mouseballs.network;

import org.huanghobbs.networkframe.GameEvent;

public class MouseEvent implements GameEvent{
	
	public int identifier;
	public float x, y, xvel, yvel, xdest, ydest;
	public boolean isPositionEvent;
	
	public MouseEvent(){}
	
	public MouseEvent(int identifier, int x, int y){
		this.isPositionEvent=true;
		this.identifier=identifier;
		this.x=x;
		this.y=y;
	}
	
	public MouseEvent(MouseBall o){
		this.isPositionEvent=false;
		this.x=o.x;
		this.y=o.y;
		this.xvel=o.xv;
		this.yvel=o.yv;
		this.xdest=o.xd;
		this.ydest=o.yd;
		
		this.identifier = o.identifier;
	}
	
	@Override
	public String toString(){
		return "MouseEvent "+this.identifier+" "+this.isPositionEvent+" "+this.x+" "+this.y;
	}

}
