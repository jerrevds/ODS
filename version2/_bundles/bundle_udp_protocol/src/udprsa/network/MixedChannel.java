package udprsa.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import udprsa.network.api.MessageReceiver;
import udprsa.network.api.NetworkChannel;
import udprsa.network.message.ROSGiMessage;
import udprsa.network.message.RemoteCallMessage;

public class MixedChannel  implements NetworkChannel {

	private MessageReceiver receiver;
	private Socket tcpSocket;
	private ObjectOutputStream tcpOutput;
	private ObjectInputStream tcpInput;
	private ByteArrayOutputStream bos;
	private DatagramSocket udpSocket;
	private ObjectOutputStream udpOutput;
	private InetAddress ip;
	private boolean connected;

	public MixedChannel(DatagramSocket socketUDP, Socket socket, MessageReceiver receiver, InetAddress ip) throws IOException{
		this.receiver = receiver;
		this.ip=ip;
		open(socketUDP, socket);
		new TCPReceiverThread().start();
		new UDPReceiverThread().start();
	}

	private void open(DatagramSocket socketUDP, Socket socket) {
		this.tcpSocket = socket;
		try {
			tcpSocket.setKeepAlive(true);
		} catch (final Throwable t) {
			// for 1.2 VMs that do not support the setKeepAlive
		}
		try {
			tcpSocket.setTcpNoDelay(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		// Use ObjectOutputstream for object serialization
		// Maybe change to a more efficient serialization algorithm?
		try {
			tcpOutput = new ObjectOutputStream(new BufferedOutputStream(
					socket.getOutputStream()));
			
			tcpOutput.flush();
			tcpInput = new ObjectInputStream(new BufferedInputStream(socket
					.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.udpSocket = socketUDP;
		bos = new ByteArrayOutputStream(256);
		try {
			udpOutput = new ObjectOutputStream(bos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MixedChannel(String ip, int port, MessageReceiver receiver) throws IOException{
		this(new DatagramSocket(port+1),new Socket(ip, port), receiver,InetAddress.getByName(ip));
		System.out.println("open mixed channel: tcp port is" + port + " udp port is " + port+1);
		
	}

	@Override
	public String getRemoteAddress() {
		return tcpSocket.getInetAddress().getHostAddress()+":"+tcpSocket.getPort();
	}

	@Override
	public String getLocalAddress() {
		return tcpSocket.getLocalAddress().getHostAddress()+":"+tcpSocket.getLocalPort();
	}

	@Override
	public void sendMessage(ROSGiMessage message) throws IOException {
		if(message instanceof RemoteCallMessage && (((RemoteCallMessage)message).isUDPEnabled())){
			message.send(udpOutput);
			byte[] buffer = bos.toByteArray();
			DatagramPacket packet =	new DatagramPacket(buffer,buffer.length,ip,udpSocket.getPort()); 
			bos.reset();
			udpSocket.send(packet);
			System.out.println("send udp");
		}else{
			System.out.println("send tcp");
			message.send(tcpOutput);
		}
		
	}

	@Override
	public void close() {
		try {
			tcpSocket.close();
		} catch(IOException ioe){

		}
		// receiver.interrupt();
		connected = false;
		
	}
	
	
	
	
	class TCPReceiverThread extends Thread {
		TCPReceiverThread() {
			setDaemon(true);
		}

		public void run() {
			while (connected) {
				try {
					
					final ROSGiMessage msg = ROSGiMessage.parse(tcpInput);
					receiver.receivedMessage(msg, MixedChannel.this);
					System.out.println("receive tcp");
				} catch (final IOException ioe) {
					connected = false;
					System.out.println("lost connection tcp");
					try {
						tcpSocket.close();
					} catch (final IOException e1) {
					}
					receiver.receivedMessage(null, MixedChannel.this);
					return;
				} catch (final Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
	
	
	class UDPReceiverThread extends Thread {
		UDPReceiverThread() {
			setDaemon(true);
		}

		public void run() {
			while (connected) {
				try {
					byte[] buffer=new byte[1250];
					DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
					udpSocket.receive(packet);
					ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData());
					ObjectInputStream input = new ObjectInputStream(bin);
					final ROSGiMessage msg = ROSGiMessage.parse(input);
					receiver.receivedMessage(msg, MixedChannel.this);
					System.out.println("receive udp");
				} catch (final IOException ioe) {
					System.out.println("lost connection udp");
					connected = false;
					udpSocket.close();
					receiver.receivedMessage(null, MixedChannel.this);
					return;
				} catch (final Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

}
