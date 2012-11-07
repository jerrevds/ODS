package udprsa.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import udprsa.network.api.MessageReceiver;
import udprsa.network.api.NetworkChannel;
import udprsa.network.message.ROSGiMessage;

public class MixedChannel  implements NetworkChannel {

	public MixedChannel(DatagramSocket socketUDP, Socket socket, MessageReceiver receiver, InetAddress ip) throws IOException{
		// TODO Auto-generated constructor stub
		//remember, +1 for the udp port
	}

	public MixedChannel(String ip, int port, MessageReceiver receiver) throws IOException{
		this(new DatagramSocket(port),new Socket(ip, port), receiver,InetAddress.getByName(ip));
	}

	@Override
	public String getRemoteAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMessage(ROSGiMessage message) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
