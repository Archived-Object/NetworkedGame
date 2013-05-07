package org.huanghobbs.networkexample.mouseballs.network;

import org.huanghobbs.networkframe.GameEvent;

public class MouseEvent implements GameEvent{
	
	public int identifier;
	public float x, y, xvel, yvel, xacc, yacc;
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
		this.x=o.px;
		this.y=o.py;
		this.xvel=o.pxv;
		this.yvel=o.pyv;
		this.xacc=o.pxa;
		this.yacc=o.pya;
		
		this.identifier = o.identifier;
	}
	
	@Override
	public String toString(){
		return "MouseEvent "+this.identifier;
	}

}
