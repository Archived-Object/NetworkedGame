package org.huanghobbs.networkexample.emptyroom;

import java.util.ArrayList;

import org.huanghobbs.networkexample.emptyroom.event.ERGameEvent;
import org.huanghobbs.networkexample.emptyroom.event.ERGameEventFactory;
import org.huanghobbs.networkexample.emptyroom.gamelogic.EmptyRoomServer;
import org.huanghobbs.networkexample.emptyroom.gamelogic.EmptyRoomSimulation;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

public class EmptyRoom extends BasicGame{

	EmptyRoomSimulation sim;
	
	public static ArrayList<String> messageQueue = new ArrayList<String> (0);

	public static long gameStart;
	
	public EmptyRoom(String title, EmptyRoomSimulation sim) {
		super(title);
		this.sim = sim;
	}

	@Override
	public void init(GameContainer gc) throws SlickException {
		//TODO loading sprites, etc
	}

	@Override
	public void update(GameContainer gc, int elapsed) throws SlickException {
		Input i = gc.getInput();
		if( i.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON) ||
				i.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON) ){
			
			//TODO looking for clicking on people
			sim.network.sendGameEvent(
					ERGameEvent.EventUpdateClient(
							sim.currentTime(),
							i.getMouseX(),
							i.getMouseY()
						)
					);
			this.sim.objects.get(this.sim.network.identifier).destx=i.getMouseX();
			this.sim.objects.get(this.sim.network.identifier).desty=i.getMouseY();
		}
		
		this.sim.tickSimulation();
		
		while(!messageQueue.isEmpty()){
			//TODO GUI text output
			System.out.println(messageQueue.remove(0));
		}
		
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		for( GameObject o: sim.objects.values()){
			o.render(g);
		}
	}
	

	
	
	public static void logMessage(String msg){
		synchronized(messageQueue){
			messageQueue.add(msg);
		}
	}
	
	
	public static void main(String[] args) throws SlickException{
		//establish game event factory
		new ERGameEventFactory();
    	
		if(args.length==0){
    		args = new String[] {"-sc"};
    	}
    	
		
		
    	if(args[0].equals("-s") || args[0].equals("-sc")  ){//run the server on -s command
    		EmptyRoomServer server = new EmptyRoomServer(); 
        	server.start();							
    	}
    	if (args[0].equals("-c") || args[0].equals("-sc") ){

    		EmptyRoomSimulation client;
    		if(args[0].equals("-c") && args.length>1){
    	    	client = new EmptyRoomSimulation(args[1]);
    		} else{
    			client = new EmptyRoomSimulation("localhost");
    		}
    		
	    	//make client in network game framework
	    	
	    	client.start();
	    	
	    	//SLICK
	    	AppGameContainer app = new AppGameContainer(new EmptyRoom("", client));
	        app.setDisplayMode(800, 600, false);
	        app.setSmoothDeltas(true);
	        app.setTargetFrameRate(60);
	        app.setShowFPS(true);
	        
	        System.out.println("STARTING APP");
	        app.start();
    	}
	}
}
