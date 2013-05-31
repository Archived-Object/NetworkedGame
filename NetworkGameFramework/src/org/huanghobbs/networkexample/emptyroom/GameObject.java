package org.huanghobbs.networkexample.emptyroom;

import org.huanghobbs.networkexample.emptyroom.event.ERGameEvent;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;


public class GameObject {
	
	//locally tracked positions and destinations.
	public float x,y,destx,desty;	
	
	//most recent update from server as to position/destination.
	//gets projected (does dead-reckoning) between updates from server.
	public float sx,sy; 
	
	//the movement speed of the game object
	protected float speed = 200;
	
	//the string name of the game object
	public String name = "no_name";
	
	//the ID of the game object
	public int id = -1;
	
	boolean onServer = false;

	long lastUpdate = 0;
	
	public GameObject(int id, float x, float y, boolean onserver){
		this.id=id;
		this.x = x;
		this.y = y;
		this.sx= x;
		this.sy= y;
		this.destx= x;
		this.desty= y;
		this.onServer=onserver;
	}
	
	public GameObject(ERGameEvent e){
		if(e.eventType==ERGameEvent.EVENT_UPDATE_C){
			this.onServer=true;
		} else{
			this.onServer=false;
		}
		this.id= (Integer) e.data[0];
		this.x = (Float) e.data[1];
		this.y = (Float) e.data[2];
		this.sx= (Float) e.data[3];
		this.sy= (Float) e.data[4];
		this.destx= (Float) e.data[5];
		this.desty= (Float) e.data[6];
	}
	
	public float getDistance(GameObject g){
		return GameObject.getDistance(this.x, this.y, g.x, g.y);
	}
	
	public static float getDistance(float x, float y, float x2, float y2){
		return (float) Math.sqrt( Math.pow(x-x2, 2)+Math.pow(y-y2, 2) );
	}

	public void update(long elapsed){
		//updating local prediction
		float hyp = getDistance(this.x, this.y, destx, desty);
		float xratio, yratio;
		if(hyp>speed*elapsed/1000){
			xratio = (destx-this.x)/hyp;
			yratio = (desty-this.y)/hyp;
			
			this.x+=xratio*speed*elapsed/1000;
			this.y+=yratio*speed*elapsed/1000;
		} else{
			this.x=destx;
			this.y=desty;
		}
		
		if(!onServer){
			//updating prediction based on server
			hyp = getDistance(this.sx, this.sy, destx, desty);
			if(hyp>speed*elapsed/1000){
				xratio = (destx-this.sx)/hyp;
				yratio = (desty-this.sy)/hyp;
				
				this.sx+=xratio*speed * elapsed/1000;
				this.sy+=yratio*speed * elapsed/1000;
			} else{
				this.sx=destx;
				this.sy=desty;
			}
			
			//smoothing the transition between the two
			hyp = getDistance(this.sx, this.sy, this.x, this.y);
			
			if(hyp>(this.sx-this.x)*elapsed/1000){
				this.x+= (this.sx-this.x)*elapsed/100;
				this.y+= (this.sy-this.y)*elapsed/100;
			}else{
				this.x=this.sx;
				this.y=this.sy;
			}
		}
	}
	
	public void updateFromServer(ERGameEvent event){
		if(	event.eventType==ERGameEvent.EVENT_UPDATE){
			this.sx= (Float) event.data[1];
			this.sy= (Float) event.data[2];
			this.destx = (Float) event.data[3] 	;
			this.desty = (Float) event.data[4];
			
		} else if ( event.eventType==ERGameEvent.EVENT_UPDATE){
			this.destx= (Float) event.data[1];
			this.desty= (Float) event.data[2];
		}
		
	}
	
	public void render(Graphics g){
		g.setColor(Color.gray);
		g.drawRect(this.sx+1, this.sy+1, 48, 48);
		g.setColor(Color.yellow);
		g.drawRect(this.destx, this.desty, 1, 1);
		g.drawRect(this.x, this.y, 50, 50);
	}

	/**
	 * assumes is update event, not clientupdate event
	 * @param e
	 * @return
	 */
	public static GameObject makeFromEvent(ERGameEvent e) {
		return new GameObject( (Integer)e.data[0], (Float)(e.data[1]), (Float)(e.data[2]), false);
	}
	
}
