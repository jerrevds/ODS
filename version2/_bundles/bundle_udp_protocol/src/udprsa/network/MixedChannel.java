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
import udprsa.network.message.RemoteCallUDPRCVMessage;
import udprsa.network.util.UDPReceiver;
import udprsa.network.util.UDPSplitterSender;

public class MixedChannel implements NetworkChannel {

	private MessageReceiver receiver;
	private Socket tcpSocket;
	private ObjectOutputStream tcpOutput;
	private ObjectInputStream tcpInput;
	private ByteArrayOutputStream bos;
	private DatagramSocket udpSocket;
	private ObjectOutputStream udpOutput;
	private InetAddress ip;
	private boolean connected = true;
	private UDPSplitterSender udpSender;
	private UDPReceiver udpReceiver;
	private boolean retrans = true;

	public MixedChannel(DatagramSocket socketUDP, Socket socket,
			MessageReceiver receiver, InetAddress ip) throws IOException {
		System.out.println("open channel:" + ip + "\n tcp: local port:"
				+ socket.getLocalPort() + " remote port: " + socket.getPort()
				+ "\n local ip: " + socket.getLocalSocketAddress().toString()
				+ " remote ip: " + socket.getRemoteSocketAddress().toString());
		this.receiver = receiver;
		this.ip = ip;
		open(socketUDP, socket);
		new TCPReceiverThread().start();
		new UDPReceiverThread().start();
		udpSender = new UDPSplitterSender(socketUDP, ip, this);
		udpReceiver = new UDPReceiver(this);
	}

	public MixedChannel(DatagramSocket socketUDP, String ip, int port,
			MessageReceiver receiver) throws IOException {
		this(socketUDP, new Socket(ip, port), receiver, InetAddress
				.getByName(ip));
		System.out.println("open mixed channel: tcp port is" + port
				+ " udp port is " + (port + 1));

	}

	private void open(DatagramSocket socketUDP, Socket socket) {
		this.tcpSocket = socket;
		try {
			tcpSocket.setKeepAlive(true);
			System.out.println("tcp alive is set");
		} catch (final Throwable t) {
			// for 1.2 VMs that do not support the setKeepAlive
		}
		try {
			tcpSocket.setTcpNoDelay(true);
			System.out.println("tcp delay is set");
		} catch (SocketException e) {
			e.printStackTrace();
		}
		// Use ObjectOutputstream for object serialization
		// Maybe change to a more efficient serialization algorithm?
		try {
			tcpOutput = new ObjectOutputStream(new BufferedOutputStream(
					tcpSocket.getOutputStream()));

			tcpOutput.flush();
			tcpInput = new ObjectInputStream(new BufferedInputStream(
					tcpSocket.getInputStream()));
			System.out.println("tcp streams are set");
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.udpSocket = socketUDP;
		bos = new ByteArrayOutputStream();
		try {
			udpOutput = new ObjectOutputStream(bos);
			System.out.println("udp streams are set");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getRemoteAddress() {
		return tcpSocket.getInetAddress().getHostAddress() + ":"
				+ tcpSocket.getPort();
	}

	@Override
	public String getLocalAddress() {
		return tcpSocket.getLocalAddress().getHostAddress() + ":"
				+ tcpSocket.getLocalPort();
	}
	
	public boolean isConnected(){
		return connected;
	}

	public boolean isRetransEnabled(){
		return retrans;
	}
	
	@Override
	public void sendMessage(ROSGiMessage message) throws IOException {
		if (message instanceof RemoteCallMessage
				&& (((RemoteCallMessage) message).isUDPEnabled())) {
			//	http://stackoverflow.com/questions/9203403/java-datagrampacket-udp-maximum-buffer-size
			// http://stackoverflow.com/questions/3997459/send-and-receive-serialize-object-on-udp-in-java)
			System.out.println("send udp");
			message.send(udpOutput);
			byte[] buffer = bos.toByteArray();
			udpSender.splitAndSend(buffer);
			//reset for headers
			bos = new ByteArrayOutputStream();
			try {
				udpOutput = new ObjectOutputStream(bos);
				System.out.println("udp streams are reset");
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("udp sended");
		} else {
			System.out.println("send tcp");
			message.send(tcpOutput);
			System.out.println("tcp sended");
		}

	}

	@Override
	public void close() {
		try {
			tcpSocket.close();
			System.out.println("tcp closed");
		} catch (IOException ioe) {

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
					//System.out.println("tcp receiving is started");
					final ROSGiMessage msg = ROSGiMessage.parse(tcpInput);
					if(msg instanceof RemoteCallUDPRCVMessage){
						RemoteCallUDPRCVMessage udprecv = (RemoteCallUDPRCVMessage)msg;
						udpSender.PacketReceived(udprecv.getId(), udprecv.getVolgnr());
						//System.out.println("receive ack");
					}else{
						receiver.receivedMessage(msg, MixedChannel.this);
						System.out.println("receive tcp");
					}
					
				} catch (final IOException ioe) {
					System.out.println("lost connection tcp");
					ioe.printStackTrace();
					connected = false;
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
					// first receive length
					byte[] data = new byte[UDPSplitterSender.FULLSIZE];
					DatagramPacket packet = new DatagramPacket(data,
							data.length);
					udpSocket.receive(packet);
					udpReceiver.received(data);
					System.out.println("received udp");
				} catch (final Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

	public void pushReady(byte[] asArray) {
		ByteArrayInputStream bin = new ByteArrayInputStream(asArray);
		ObjectInputStream input;
		ROSGiMessage msg;
		try {
			input = new ObjectInputStream(bin);
			msg = ROSGiMessage.parse(input);
			receiver.receivedMessage(msg, this);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("lost connection udp");
			connected = false;
			udpReceiver.clear();
			udpSocket.close();
			receiver.receivedMessage(null, MixedChannel.this);
			e.printStackTrace();
		}
		
	}

}
