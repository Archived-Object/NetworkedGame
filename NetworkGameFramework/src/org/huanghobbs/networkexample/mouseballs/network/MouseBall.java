package org.huanghobbs.networkexample.mouseballs.network;

/**
 * A basic game object that has 2d physics and stuff.
 * @author Maxwell
 *
 */
public class MouseBall {
	
	public static final float correctionFactor =2.0F; //higher = faster correction to server value.

	public boolean controlledLocal = false;
	
	public int identifier;
	public float px, py, pxv, pyv, pxa, pya; //display predicted physics variables (locally determined)
	public float sx, sy, sxv, syv; //server based predicted physics variables (server-determined)
	
	public MouseBall(int x, int y, boolean controlledLocal){
		this.px=x;
		this.py=y;
		this.sx=px;
		this.sy=py;
		this.controlledLocal = controlledLocal;
	}
	
	public MouseBall(MouseEvent e) {
		this.px=e.x;
		this.py=e.y;
		this.pxv=e.xvel;
		this.pyv=e.yvel;
		this.pxa=e.xacc;
		this.pya=e.yacc;
		//==
		this.sx=e.x;
		this.sy=e.y;
		//==
		this.identifier=e.identifier;
	}

	public void update(int elapsed){
		//updating local prediction based on PHYSICS
		this.pxv+=pxa;
		this.pyv+=pya;
		this.px+=pxv;
		this.py+=pyv;
		
		//updating more correct local prediction based on PHYSICS
		this.sx+=sxv;
		this.sy+=sxv;
		
		//correcting display to match server
		this.px = this.px + (this.sx - this.px)*(correctionFactor)*elapsed/1000;
		this.py = this.py + (this.sy - this.py)*(correctionFactor)*elapsed/1000;
	}
	
	public void updateFromServer(MouseEvent pev2d){
		this.sx  = pev2d.x;
		this.sy  = pev2d.y;
		this.pxv = pev2d.xvel;
		this.pyv = pev2d.yvel;
	}

}
