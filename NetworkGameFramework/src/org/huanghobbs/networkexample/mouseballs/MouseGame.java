package org.huanghobbs.networkexample.mouseballs;

import java.util.ArrayList;

import org.huanghobbs.networkexample.mouseballs.network.GameObject;
import org.huanghobbs.networkexample.mouseballs.network.PhysicsEvent2D;
import org.huanghobbs.networkexample.mouseballs.network.PhysicsEvent2DFactory;
import org.huanghobbs.networkframe.client.ClientSimulation;
import org.huanghobbs.networkframe.server.ServerGameplay;
import org.huanghobbs.networkframe.server.WrappedClient;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

/**
 * "Game" in which you have a tiny ball that follows your mouse
 * @author Maxwell
 *
 */
public class MouseGame extends BasicGame {
 
    static final int width = 640;
    static final int height = 480;
   
    static final boolean fullscreen = false;
    static final boolean showFPS = true;
    static final String title = "MOUSEGAME";
    static final int fpslimit = 60;
    
    MouseClient manager;
    
    public MouseGame(String title, MouseClient manager) {
        super(title);
        this.manager = manager;
    }
 
    @Override
    public void init(GameContainer gc) throws SlickException {}
 
    @Override
    public void update(GameContainer gc, int delta) throws SlickException {
    	this.manager.network.sendGameEvent(new PhysicsEvent2D( this.manager.network.identifier, gc.getInput().getMouseX(), gc.getInput().getMouseY() ));
    	//send mouse position updates
    }
 
    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {
    	g.setColor(Color.black);
    	g.fillRect(0,0,gc.getWidth(),gc.getHeight());
    	for(GameObject o: manager.objects){
    		if(o.identifier==this.manager.network.identifier){
    	    	g.setColor(Color.cyan);
    		}else{
    	    	g.setColor(Color.gray);
    		}
    		g.fillOval(o.px-5, o.py-5, 10, 10);
    	}
    }
   
    public static void main(String[] args) throws SlickException {
    	//establish event protocol
    	new PhysicsEvent2DFactory();
    	if(args.length==0){
    		args = new String[] {"-sc"};
    	}
    	
    	if(args[0].equals("-s") || args[0].equals("-sc")  ){//run the server on -s command
    		MouseServer server = new MouseServer(); 
        	server.start();							
    	}
    	if (args[0].equals("-c") || args[0].equals("-sc") ){

    		MouseClient client;
    		if(args[0].equals("-c") && args.length>1){
    	    	client = new MouseClient(args[1]);
    		} else{
    			client = new MouseClient("localhost");
    		}
    		
	    	//make client in network game framework
	    	
	    	client.start();
	    	
	    	//SLICK
	    	AppGameContainer app = new AppGameContainer(new MouseGame(title, client));
	        app.setDisplayMode(width, height, fullscreen);
	        app.setSmoothDeltas(true);
	        app.setTargetFrameRate(fpslimit);
	        app.setShowFPS(showFPS);
	        app.start();
    	}
    }
   
}




/**
 * the client half
 * @author Maxwell
 *
 */
class MouseClient extends ClientSimulation<PhysicsEvent2D>{
	
	public ArrayList<GameObject> objects = new ArrayList<GameObject>(0);
	
	public MouseClient(String addr){
		super(addr);
	}
	
	@Override
	public void tickSimulation(){
		for(GameObject object: objects){
			object.update(elapsed);//predict the positions of all the objects
		}
	}

	@Override
	/**
	 * applies an event from the parent simulation
	 */
	public void handleEvent(PhysicsEvent2D e) {
		boolean handled=false;
		for(int i=0; i<this.objects.size(); i++){
			if(this.objects.get(i).identifier == e.identifier){
				this.objects.get(i).updateFromServer(e);
				handled=true;
				break;
			}
		}
		if(!handled){
			this.objects.add(new GameObject(e));
		}
	}
	
} 


/**
 * the server half
 * @author Maxwell
 *
 */
class MouseServer extends ServerGameplay<PhysicsEvent2D>{

	int recentDigit;
	boolean advanced = true;
	
	ArrayList<GameObject> gameObjects = new ArrayList<GameObject>(0);
	
	static final float ballSpeed = 10F;
	
	@Override
	public boolean handleEvent(PhysicsEvent2D e, WrappedClient<PhysicsEvent2D> source) {
		if(e.identifier==source.identifier && e.isPositionEvent){
			GameObject target = null;
			for(int i=0; i<this.gameObjects.size(); i++){
				if(this.gameObjects.get(i).identifier == e.identifier){
					target = this.gameObjects.get(i);
					break;
				}
			}
			target.pxa = (e.x-target.px)/100;
			target.pya = (e.y-target.py)/100;
		}
		return false;
	}
	
	@Override
	public void tickUniverse(){
		
		for(GameObject object: gameObjects){
			object.update(elapsed);
			this.network.dispatchEvent(new PhysicsEvent2D(object));
		}
	}

	@Override
	public void onConnect(WrappedClient<PhysicsEvent2D> justConnected) {
		GameObject o = new GameObject(0,0,true);//spawn a new object when someone connects
		this.gameObjects.add(o);
		o.identifier = justConnected.identifier;
		this.network.dispatchEvent(new PhysicsEvent2D(o));
	}

	@Override
	public void onDisconnect(WrappedClient<PhysicsEvent2D> justDropped) {
		for(int i=0; i<this.gameObjects.size(); i++){
			if(this.gameObjects.get(i).identifier == justDropped.identifier){
				this.gameObjects.remove(this.gameObjects.get(i));
			}
		}
	}
	
}