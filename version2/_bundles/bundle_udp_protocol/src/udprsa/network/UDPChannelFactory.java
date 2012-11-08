package udprsa.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import udprsa.network.api.MessageReceiver;
import udprsa.network.api.NetworkChannel;
import udprsa.network.api.NetworkChannelFactory;
import udprsa.util.URI;

public class UDPChannelFactory  implements NetworkChannelFactory{



	
	private Map<String, NetworkChannel> channels = new HashMap<String, NetworkChannel>();
	
	private MessageReceiver receiver;
	
	public UDPChannelFactory(MessageReceiver receiver, String networkInterface, int port){
		this.receiver = receiver;
	
	}
	
	public void activate() throws IOException {
		//nothing to do here, udp doesn't have to accept
	}

	public void deactivate(){
	
		synchronized(channels){
			for(NetworkChannel channel : channels.values()){
				channel.close();
			}
			channels.clear();
		}
	}
	
	@Override
	public NetworkChannel getChannel(URI uri) {
		synchronized(channels){
			NetworkChannel channel = channels.get(uri.getAddress());
			if(channel == null) {
				try {
					//do port + 1 because tcp listens to the normal port
					channel = new UDPChannel(InetAddress.getByName(uri.getIP()), uri.getPort()+1, receiver);
					channels.put(channel.getRemoteAddress(), channel);
				} catch(IOException ioe){
					System.out.println("Failed to create UDP connection to "+uri);
					ioe.printStackTrace();
				}
			}
			return channel;
		}
	}

	public void deleteChannel(NetworkChannel channel){
		synchronized(channels){
			channels.remove(channel.getRemoteAddress());
			channel.close();
		}
	}
	
	
	
	@Override
	public String getAddress() {
		return null;
	}

}
