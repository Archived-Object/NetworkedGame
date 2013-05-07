package org.huanghobbs.networkexample.mouseballs;

import java.util.ArrayList;

import org.huanghobbs.networkexample.mouseballs.network.MouseBall;
import org.huanghobbs.networkexample.mouseballs.network.MouseEvent;
import org.huanghobbs.networkexample.mouseballs.network.MouseEventFactory;
import org.huanghobbs.networkframe.client.ClientSimulation;
import org.huanghobbs.networkframe.server.ServerGameplay;
import org.huanghobbs.networkframe.server.WrappedClient;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
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
    public void init(GameContainer gc) throws SlickException {
    	Image cursor  = new Image(2,2);
    	cursor.setAlpha(0);
    	gc.setMouseCursor(cursor, 0,0);
    }
 
    @Override
    public void update(GameContainer gc, int delta) throws SlickException {
    	this.manager.network.sendGameEvent(
    			new MouseEvent(
    					this.manager.network.identifier,
    					gc.getInput().getMouseX(),
    					gc.getInput().getMouseY()
    				)
    			);
    	//send mouse position updates
    }
 
    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {
    	
    	manager.checkOutVariable("objects");
    	for(MouseBall o: manager.objects){
    		if(o.identifier==this.manager.network.identifier){
    	    	g.setColor(Color.white);
    	    	g.drawLine(o.x, o.y, gc.getInput().getMouseX(), gc.getInput().getMouseY());
    	    	g.setColor(Color.cyan);
    		}else{
    	    	g.setColor(Color.gray);
    		}
    		g.fillOval(o.x-5, o.y-5, 10, 10);
    	}
    	manager.releaseVariable("objects");
    }
   
    public static void main(String[] args) throws SlickException {
    	//establish event protocol
    	
    	new MouseEventFactory();
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
	        
	        System.out.println("STARTING APP");
	        app.start();
    	}
    }
   
}




/**
 * the client half
 * @author Maxwell
 *
 */
class MouseClient extends ClientSimulation<MouseEvent>{
	
	public ArrayList<MouseBall> objects = new ArrayList<MouseBall>(0);
	
	public MouseClient(String addr){
		super(addr);
	}
	
	@Override
	public void tickSimulation(){
		this.checkOutVariable("objects");
		for(MouseBall object: objects){
			object.update(elapsed);//predict the positions of all the objects
		}
		this.releaseVariable("objects");
	}

	@Override
	/**
	 * applies an event from the parent simulation
	 */
	public void handleEvent(MouseEvent e) {
		this.checkOutVariable("objects");
		boolean handled=false;
		for(int i=0; i<this.objects.size(); i++){
			if(this.objects.get(i).identifier == e.identifier){
				this.objects.get(i).updateFromServer(e);
				handled=true;
				break;
			}
		}
		if(!handled){
			this.objects.add(new MouseBall(e));
		}
		this.releaseVariable("objects");
	}
	
} 


/**
 * the server half
 * @author Maxwell
 *
 */
class MouseServer extends ServerGameplay<MouseEvent>{

	int recentDigit;
	boolean advanced = true;
	
	ArrayList<MouseBall> mouseBalls = new ArrayList<MouseBall>(0);
	
	static final float ballSpeed = 10F;
	
	@Override
	public boolean handleEvent(MouseEvent e, WrappedClient<MouseEvent> source) {
		if(e.identifier==source.identifier && e.isPositionEvent){
			this.checkOutVariable("gameObjects");
			for(int i=0; i<this.mouseBalls.size(); i++){
				if(this.mouseBalls.get(i).identifier == e.identifier){
					this.mouseBalls.get(i).updateFromServer(e);
					break;
				}
			}
			this.releaseVariable("gameObjects");
		}
		return false;
	}
	
	@Override
	public void tickUniverse(){
		this.checkOutVariable("gameObjects");
		for(MouseBall object: mouseBalls){
			object.update(elapsed);
			this.network.dispatchEvent(new MouseEvent(object));
		}
		this.releaseVariable("gameObjects");
	}

	@Override
	public void onConnect(WrappedClient<MouseEvent> justConnected) {
		this.checkOutVariable("gameObjects");
		MouseBall o = new MouseBall(0,0);//spawn a new object when someone connects
		o.identifier = justConnected.identifier;
		this.mouseBalls.add(o);

		System.out.println("new object, id "+o.identifier);
		System.out.println("Server thinks is "+o.identifier);
		MouseEvent e = new MouseEvent(o);
		System.out.println(e.identifier);
		this.network.dispatchEvent(e);
		this.releaseVariable("gameObjects");
	}

	@Override
	public void onDisconnect(WrappedClient<MouseEvent> justDropped) {
		this.checkOutVariable("gameObjects");
		for(int i=0; i<this.mouseBalls.size(); i++){
			if(this.mouseBalls.get(i).identifier == justDropped.identifier){
				this.mouseBalls.remove(this.mouseBalls.get(i));
			}
		}
		this.releaseVariable("gameObjects");
	}
	
}