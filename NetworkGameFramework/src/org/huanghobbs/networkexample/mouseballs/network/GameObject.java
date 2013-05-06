package org.huanghobbs.networkexample.mouseballs.network;

/**
 * A basic game object that has 2d physics and stuff.
 * @author Maxwell
 *
 */
public class GameObject {
	
	public static final float correctionFactor =0.5F; //higher = faster correction to server value.

	public boolean controlledLocal = false;
	
	public int identifier;
	public float px, py, pxv, pyv, pxa, pya; //display predicted physics variables (locally determined)
	public float sx, sy, sxv, syv, sxa, sya; //server based predicted physics variables (server-determined)
	
	public GameObject(int x, int y, boolean controlledLocal){
		this.px=x;
		this.py=y;
		this.sx=px;
		this.sy=py;
		this.controlledLocal = controlledLocal;
	}
	
	public GameObject(PhysicsEvent2D e) {
		this.px=e.x;
		this.py=e.y;
		this.pxv=e.xvel;
		this.pyv=e.yvel;
		this.pxa=e.xacc;
		this.pya=e.yacc;
		//==
		this.sx=e.x;
		this.sy=e.y;
		this.sxv=e.xvel;
		this.syv=e.yvel;
		this.sxa=e.xacc;
		this.sya=e.yacc;
	}

	public void update(int elapsed){
		//updating local prediction based on PHYSICS
		this.pxv+=pxa;
		this.pyv+=pya;
		this.px+=pxv;
		this.py+=pyv;
		
		//updating more correct local prediction based on PHYSICS
		this.sxv+=sxa;
		this.syv+=sya;
		this.sx+=sxv;
		this.sy+=sxv;
		
		//correcting display position to match server position
		this.px = this.px + (this.sx - this.px)*(correctionFactor)*elapsed/1000;
		this.py = this.py + (this.sy - this.py)*(correctionFactor)*elapsed/1000;
	}
	
	public void updateFromServer(PhysicsEvent2D pev2d){
		this.sx  = pev2d.x;
		this.sy  = pev2d.y;
		this.sxv = pev2d.xvel;
		this.sxv = pev2d.yvel;
		this.sxa = pev2d.xacc;
		this.sya = pev2d.yacc;
	}

}
