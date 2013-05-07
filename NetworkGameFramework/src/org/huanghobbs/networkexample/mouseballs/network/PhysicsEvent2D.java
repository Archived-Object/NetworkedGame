package org.huanghobbs.networkexample.mouseballs.network;

import org.huanghobbs.networkframe.GameEvent;

public class PhysicsEvent2D implements GameEvent{
	
	public int identifier;
	public float x, y, xvel, yvel, xacc, yacc;
	public boolean isPositionEvent;
	
	public PhysicsEvent2D(){}
	
	public PhysicsEvent2D(int identifier, int x, int y){
		this.isPositionEvent=true;
		this.identifier=identifier;
		this.x=x;
		this.y=y;
	}
	
	public PhysicsEvent2D(GameObject o){
		this.isPositionEvent=false;
		this.x=o.px;
		this.y=o.py;
		this.xvel=o.pxv;
		this.yvel=o.pyv;
		this.xacc=o.pxa;
		this.yacc=o.pya;
		
		this.identifier = o.identifier;
	}
	
	public String toString(){
		return "PhysEvent "+this.identifier;
	}

}
