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

	@Override
	public void sendMessage(ROSGiMessage message) throws IOException {
		if (message instanceof RemoteCallMessage
				&& (((RemoteCallMessage) message).isUDPEnabled())) {
			System.out.println("send udp");
			message.send(udpOutput);
			byte[] buffer = bos.toByteArray();
			
			// first send length (code from
			// http://stackoverflow.com/questions/3997459/send-and-receive-serialize-object-on-udp-in-java)
			int number = buffer.length;
			byte[] data = new byte[4];

			// int -> byte[]
			for (int i = 0; i < 4; ++i) {
				int shift = i << 3; // i * 8
				data[3 - i] = (byte) ((number & (0xff << shift)) >>> shift);
			}
			DatagramPacket packet = new DatagramPacket(data, 4, ip,
					udpSocket.getLocalPort());
			udpSocket.send(packet);

			// then send payload
			packet = new DatagramPacket(buffer, buffer.length, ip,
					udpSocket.getLocalPort());

			udpSocket.send(packet);
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
					System.out.println("tcp receiving is started");
					final ROSGiMessage msg = ROSGiMessage.parse(tcpInput);
					receiver.receivedMessage(msg, MixedChannel.this);
					System.out.println("receive tcp");
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
					byte[] data = new byte[4];
					DatagramPacket packet = new DatagramPacket(data,
							data.length);
					udpSocket.receive(packet);

					int len = 0;
					// byte[] -> int
					for (int i = 0; i < 4; ++i) {
						len |= (data[3 - i] & 0xff) << (i << 3);
					}

					// now we know the length of the payload
					byte[] buffer = new byte[len];
					// then data
					packet = new DatagramPacket(buffer, buffer.length);
					udpSocket.receive(packet);
					ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData());
					ObjectInputStream input = new ObjectInputStream(bin);
					final ROSGiMessage msg = ROSGiMessage.parse(input);
					receiver.receivedMessage(msg, MixedChannel.this);
					System.out.println("receive udp");
				} catch (final IOException ioe) {
					ioe.printStackTrace();
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
