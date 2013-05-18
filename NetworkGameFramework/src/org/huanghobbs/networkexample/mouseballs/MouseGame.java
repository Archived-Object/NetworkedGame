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
    
    float opacity = 1.0F;
    int oldx=0;
    int oldy=0;
    
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
    	gc.setAlwaysRender(true);
    	oldx=gc.getInput().getMouseX();
    	oldy=gc.getInput().getMouseY();
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
    	opacity-=delta/750F;
    	if( Math.abs(gc.getInput().getMouseX()-oldx)>5 || Math.abs(oldy-gc.getInput().getMouseY())>5 ){
    		opacity=1.0F;
    	}
    	oldx=gc.getInput().getMouseX();
    	oldy=gc.getInput().getMouseY();
    	
    	//send mouse position updates
    }
 
    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {
    	
	    synchronized(this.manager.objects ){
	    	for(MouseBall o: manager.objects){
	    		if(o.identifier==this.manager.network.identifier){
	    	    	float xo = gc.getInput().getMouseX()-o.x;
	    	    	float yo = gc.getInput().getMouseY()-o.y;
	    	    	
	    	    	float hyp = (float)Math.sqrt(xo*xo+yo*yo);
	    	    	if(hyp>255){
	    	    		hyp=255;
	    	    	}
	    	    	float frac = hyp/255;
	    	    	float op = frac*opacity;
	    	    	
	    	    	g.setColor(new Color(op,op,op,op));
	    	    	for(int i=1; i<=10; i++){
	    	    		g.setLineWidth(2-i/10F);
	    	    		g.drawLine(o.x, o.y, o.x+xo*i/10, o.y+yo*i/10);
	    	    	}
	    	    	g.setColor(Color.cyan);
	    		}else{
	    	    	g.setColor(Color.gray);
	    		}
	    		g.fillOval(o.x-5, o.y-5, 10, 10);
	    	}
	    }
    }
   
    public static void main(String[] args) throws SlickException {
    	//establish event protocol
    	
    	new MouseEventFactory();
    	if(args.length==0){
    		args = new String[] {"-c","98.114.254.50"};
    	}
    	
    	if(args[0].equals("-s") || args[0].equals("-sc")  ){//run the server on -s command
    		System.out.println("GOSERVER");
    		MouseServer server = new MouseServer(); 
        	server.start();							
    	}
    	if (args[0].equals("-c") || args[0].equals("-sc") ){
    		System.out.println("GOCLIENT");
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
		synchronized(this.objects ){
			for(MouseBall object: objects){
				object.update(elapsed);//predict the positions of all the objects
			}
		}
	}

	@Override
	/**
	 * applies an event from the parent simulation
	 */
	public void handleEvent(MouseEvent e) {
		synchronized(this.objects ){
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
				System.out.println("client spawning ball "+e);
			}
		}
	}
	
	@Override
	public void start(){
		super.start();
		this.startNetwork();
	}
	
	@Override
	public void onDisconnect(){
		
	}
	

	@Override
	public void onReconnect(){
		
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
	
	public MouseServer(){
		super();
		this.TickTime=10;//super low tick time
	}
	
	@Override
	public boolean handleEvent(MouseEvent e, WrappedClient<MouseEvent> source) {
		if(e.identifier==source.identifier && e.isPositionEvent){
			synchronized(this.mouseBalls){
				for(int i=0; i<this.mouseBalls.size(); i++){
					if(this.mouseBalls.get(i).identifier == e.identifier){
						this.mouseBalls.get(i).updateFromServer(e);
						break;
					}
				}
			}
		}
		return true;//treat all events as valid
	}
	
	@Override
	public void tickUniverse(){
		synchronized(this.mouseBalls){
			for(MouseBall object: mouseBalls){
				object.update(elapsed);
				this.network.dispatchEvent(new MouseEvent(object));
			}
		}
	}

	@Override
	public void onConnect(WrappedClient<MouseEvent> justConnected) {
		synchronized (this.mouseBalls){
			MouseBall o = new MouseBall(20,20);//spawn a new object when someone connects
			o.identifier = justConnected.identifier;
			this.mouseBalls.add(o);
	
			System.out.println("new object, id "+o.identifier);
			MouseEvent e = new MouseEvent(o);
			this.network.dispatchEvent(e);
		}
	}

	@Override
	public void onDisconnect(WrappedClient<MouseEvent> justDropped) {
		System.out.println("Client Disconnect");
	}
	
	@Override
	public void onReconnect(WrappedClient<MouseEvent> reconnected){
		System.out.println("Client Reconnected");
	}
	
}