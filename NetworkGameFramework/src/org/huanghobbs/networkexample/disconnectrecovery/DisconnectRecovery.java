package org.huanghobbs.networkexample.disconnectrecovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.huanghobbs.networkexample.randomnumber.IntGameEvent;
import org.huanghobbs.networkexample.randomnumber.IntGameEventFactory;
import org.huanghobbs.networkframe.client.ClientSimulation;
import org.huanghobbs.networkframe.server.ServerGameplay;
import org.huanghobbs.networkframe.server.WrappedClient;

public class DisconnectRecovery {

	DisconnectRecoveryServer server;
	DisconnectRecoveryClient client;
	
	public static void main(String[] args) throws IOException{
		if(args.length==0){
			args=new String[] {"localhost"};
		}
		
		new IntGameEventFactory();
		
		new DisconnectRecovery(args[0], true, true);
	}
	
	public DisconnectRecovery (String addr, boolean client, boolean server) throws IOException {
		if(server){
			this.server = new DisconnectRecoveryServer();
			this.server.start();
		}
		if(client){
			this.client = new DisconnectRecoveryClient(addr);
			this.client.start();
			this.doGame();
		}
	}
	
	public void doGame() throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("what will your fingerprint be?");
		String in = br.readLine();
		
		this.client.network.setFingerprint(in);
		
		System.out.println("commands are 'connect' and 'disconnect'");
		while(true){
			in = br.readLine();
			System.out.println("  >"+in);
			
			if( in.equalsIgnoreCase("disconnect") && this.client.network.isConnected() ){
				this.client.network.stop();
			} else if (in.equalsIgnoreCase("connect") && !this.client.network.isConnected()){
				this.client.network.start();
			} else{
				System.out.println("what?");
			}
			
		}
	}
	
}

class DisconnectRecoveryClient extends ClientSimulation<IntGameEvent>{

	public DisconnectRecoveryClient(String targetAddress) {
		super(targetAddress);
	}

	@Override
	public void handleEvent(IntGameEvent e) {
		//System.out.print(e.value);
	}

	@Override
	public void onDisconnect() {
		System.out.println("disconnected.");
	}

	@Override
	public void onReconnect() {
		System.out.println("reconnected.");
	}
	
}

class DisconnectRecoveryServer extends ServerGameplay<IntGameEvent>{

	int counter = 1;
	int netelapsed = 0;
	
	private static final int tickincriment = 1000;
	
	@Override
	public void tickUniverse(){
		super.tickUniverse();
		this.netelapsed+=this.elapsed;
		
		while(this.netelapsed>tickincriment){
			this.netelapsed -= tickincriment;
			this.counter = (this.counter%99)+1;
			this.network.dispatchEvent( new IntGameEvent(this.counter) );
		}
	}
	
	@Override
	public boolean handleEvent(IntGameEvent e,
			WrappedClient<IntGameEvent> source) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onConnect(WrappedClient<IntGameEvent> justConnected) {
		System.out.println("client "+justConnected.identifier+" connected for the first time.");
		
	}

	@Override
	public void onDisconnect(WrappedClient<IntGameEvent> justDropped) {
		System.out.println("client "+justDropped.identifier+" disconnected.");
	}

	@Override
	public void onReconnect(WrappedClient<IntGameEvent> justConnected) {
		System.out.println("client "+justConnected.identifier+" reconnected.");
	}
	
}