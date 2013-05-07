package org.huanghobbs.networkexample.mouseballs.network;

/**
 * A basic game object that has 2d physics and stuff.
 * @author Maxwell
 *
 */
public class MouseBall {
	
	public static final float correctionFactor =2.0F; //higher = faster correction to server value.
	public static final float decay =0.3F; //higher = faster decay of velocity.

	
	public int identifier;
	public float x, y, xv, yv; //physics
	public float xs, ys; //where server is
	public float xd, yd; //where go
	
	public MouseBall(int x, int y){
		this.x=x;
		this.y=y;
	}
	
	public MouseBall(MouseEvent e) {
		this.xs=e.x;
		this.ys=e.y;
		this.xv=e.xvel;
		this.yv=e.yvel;
		this.xd=e.xdest;
		this.yd=e.ydest;
		this.identifier=e.identifier;
	}

	public void update(int elapsed){
		//updating local prediction
		this.xv += (this.xd-this.x)/70*elapsed/1000;
		this.yv += (this.yd-this.y)/70*elapsed/1000;
		
		this.xv=this.xv*(1-(decay)*elapsed/1000);
		this.yv=this.yv*(1-(decay)*elapsed/1000);
		
		this.x+=xv;
		this.y+=yv;
		this.xs+=xv;
		this.ys+=yv;
		
		//correcting display to match server over time
		this.x = this.x + (this.xs - this.x)*(correctionFactor)*elapsed/1000;
		this.y = this.y + (this.ys - this.y)*(correctionFactor)*elapsed/1000;
	}
	
	public void updateFromServer(MouseEvent mev){
		if(!mev.isPositionEvent){
			this.xs = mev.x;
			this.ys = mev.y;
			this.xv = mev.xvel;
			this.yv = mev.yvel;
			this.xd = mev.xdest;
			this.yd = mev.ydest;
		} else{
			this.xd = mev.x;
			this.yd = mev.y;
		}
	}
	
	@Override
	public String toString(){
		return "Ball "+this.x+" "+ this.y+" "+this.xv+" "+ this.yv+this.xd+" "+ this.yd;
	}

}
