package udprsa.network;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import udprsa.network.api.MessageReceiver;
import udprsa.network.api.NetworkChannel;
import udprsa.network.api.NetworkChannelFactory;
import udprsa.util.URI;

public class MixedChannelFactory implements  NetworkChannelFactory{


	private String networkInterface = null;
	//one more than the tim-tcp port
	private int listeningPortTcp = 9279;
	private int listeningPortUdp = 9280;
	private MixedAcceptorThread thread;
	
	private Map<String, NetworkChannel> channels = new HashMap<String, NetworkChannel>();
	
	private MessageReceiver receiver;
	
	public MixedChannelFactory(MessageReceiver receiver, String networkInterface, int port){
		this.receiver = receiver;
		this.networkInterface = networkInterface;
		if(port!=-1){
			this.listeningPortTcp = port;
			//udp one higher than tcp
			this.listeningPortUdp = port+1;
		}
	}
	
	public void activate() throws IOException {
		thread = new MixedAcceptorThread();
		thread.start();
	}

	public void deactivate(){
		thread.interrupt();
		
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
					
					channel = new MixedChannel(uri.getIP(), uri.getPort(), receiver);
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
	
	
	// handles incoming udp and tcp messages.
	protected final class MixedAcceptorThread extends Thread {

		private ServerSocket socketTCP;
		private DatagramSocket socketUDP;
		MixedAcceptorThread() throws IOException {
			setDaemon(true);

			int e = 0;
			boolean searching=true;
			while (searching) {
				try {
					listeningPortTcp += e;
					socketTCP = new ServerSocket(listeningPortTcp);
					return;
				} catch (final BindException b) {
					e++;
				}
			}

			
		}
		


		public void run() {
			while (!isInterrupted()) {
				try {
					// accept incoming connections
					Socket s = socketTCP.accept();
					MixedChannel channel = new MixedChannel(socketUDP, s, receiver, s.getInetAddress());
					synchronized(channels){
						channels.put(channel.getRemoteAddress(), channel);
					}
				} catch (IOException ioe) {
					System.out.println("Failed to accept connection");
					ioe.printStackTrace();
				}
			}
			
		}
		
		// method to try to get a currently valid ip of the host
		public String getListeningAddress(){
			String hostAddress = null;
			try {
				Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
				for (NetworkInterface netint : Collections.list(nets)){
					Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
					for (InetAddress inetAddress : Collections.list(inetAddresses)) {
						 if(inetAddress instanceof Inet4Address){
							 if(hostAddress!=null && (inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress()))
								 break;  //only set loopbackadres if no other possible
							 else {	 
								 hostAddress = inetAddress.getHostAddress();
					     		 break;
							 }
					     }
					}
					if(netint.getName().equals(networkInterface) && hostAddress!=null){ // prefer configured networkInterface
						break;
					}
			    }
			}catch(Exception e){}
			//System.out.println(hostAddress);
			if(hostAddress==null)
				hostAddress = socketTCP.getInetAddress().getHostAddress();
			
			return hostAddress+":"+socketTCP.getLocalPort();
		}
	}

	@Override
	public String getAddress() {
		if(thread!=null)
			return thread.getListeningAddress();
		return null;
	}

}
