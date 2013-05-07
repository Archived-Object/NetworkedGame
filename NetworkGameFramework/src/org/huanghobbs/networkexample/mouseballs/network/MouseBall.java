package org.huanghobbs.networkexample.mouseballs.network;

/**
 * A basic game object that has 2d physics and stuff.
 * @author Maxwell
 *
 */
public class MouseBall {
	
	public static final float correctionFactor =2.0F; //higher = faster correction to server value.
	
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

		this.xv += (this.xd-this.x)/100;
		this.yv += (this.yd-this.y)/100;
		
		this.x+=xv;
		this.y+=yv;
		this.xd+=xv;
		this.yd+=yv;
		
		//correcting display to match server over time
		this.x = this.x + (this.xs - this.x)*(correctionFactor)*elapsed/1000;
		this.y = this.y + (this.ys - this.y)*(correctionFactor)*elapsed/1000;
	}
	
	public void updateFromServer(MouseEvent mev){
		if(!mev.isPositionEvent){
			this.xv = mev.xvel;
			this.yv = mev.yvel;
			this.xd = mev.xdest;
			this.yd = mev.ydest;
		}
	}

}
