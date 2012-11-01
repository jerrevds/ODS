package udprsa.network;

import java.io.IOException;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import udprsa.network.api.MessageReceiver;
import udprsa.network.api.NetworkChannel;
import udprsa.network.api.NetworkChannelFactory;
import udprsa.util.URI;

/*
 * Factory for creating TCP Channels
 */
public class TCPChannelFactory implements NetworkChannelFactory{

	private String networkInterface = null;
	private int listeningPort = 9278;
	private TCPAcceptorThread thread;
	
	private Map<String, NetworkChannel> channels = new HashMap<String, NetworkChannel>();
	
	private MessageReceiver receiver;
	
	public TCPChannelFactory(MessageReceiver receiver, String networkInterface, int port){
		this.receiver = receiver;
		this.networkInterface = networkInterface;
		if(port!=-1)
			this.listeningPort = port;
	}
	
	public void activate() throws IOException {
		thread = new TCPAcceptorThread();
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
					channel = new TCPChannel(uri.getIP(), uri.getPort(), receiver);
					channels.put(channel.getRemoteAddress(), channel);
				} catch(IOException ioe){
					System.out.println("Failed to create TCP connection to "+uri);
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
	
	
	// handles incoming tcp messages.
	protected final class TCPAcceptorThread extends Thread {

		private ServerSocket socket;

		TCPAcceptorThread() throws IOException {
			setDaemon(true);

			int e = 0;
			while (true) {
				try {
					listeningPort += e;
					socket = new ServerSocket(listeningPort);
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
					TCPChannel channel = new TCPChannel(socket.accept(), receiver);
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
				hostAddress = socket.getInetAddress().getHostAddress();
			
			return hostAddress+":"+socket.getLocalPort();
		}
	}

	@Override
	public String getAddress() {
		if(thread!=null)
			return thread.getListeningAddress();
		return null;
	}
}
