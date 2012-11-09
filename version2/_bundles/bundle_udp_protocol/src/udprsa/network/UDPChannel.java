package udprsa.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import udprsa.network.api.MessageReceiver;
import udprsa.network.api.NetworkChannel;
import udprsa.network.message.ROSGiMessage;

public class UDPChannel implements NetworkChannel {

	private MessageReceiver receiver;
	private boolean connected;
	private DatagramSocket socket;
	private ByteArrayOutputStream bos;
	private ObjectOutputStream out;
	private InetAddress ip;

	public UDPChannel(InetAddress ip, int port, MessageReceiver receiver)
			throws IOException {
		this(new DatagramSocket(port), receiver,ip);
	}

	private UDPChannel(DatagramSocket socket, MessageReceiver receiver, InetAddress ip)
			throws IOException {
		this.ip = ip;
		this.receiver = receiver;
		open(socket);
		new ReceiverThread().start();
	}

	private void open(DatagramSocket socket) throws IOException {
		this.socket = socket;
		bos = new ByteArrayOutputStream(256);
		out = new ObjectOutputStream(bos);

	}

	@Override
	public String getRemoteAddress() {
		return socket.getInetAddress().getHostAddress() + ":"
				+ socket.getPort();
	}

	@Override
	public String getLocalAddress() {
		return socket.getLocalAddress().getHostAddress() + ":"
				+ socket.getLocalPort();
	}

	@Override
	public void sendMessage(ROSGiMessage message) throws IOException {
		message.send(out);
		byte[] buffer = bos.toByteArray();
		DatagramPacket packet =	new DatagramPacket(buffer,buffer.length,ip,socket.getPort()); 
		bos.reset();
		socket.send(packet);

	}

	@Override
	public void close() {
		socket.close();
		connected = false;

	}

	class ReceiverThread extends Thread {
		ReceiverThread() {
			setDaemon(true);
		}

		public void run() {
			while (connected) {
				try {
					byte[] buffer=new byte[1250];
					DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
					socket.receive(packet);
					ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData());
					ObjectInputStream input = new ObjectInputStream(bin);
					final ROSGiMessage msg = ROSGiMessage.parse(input);
					receiver.receivedMessage(msg, UDPChannel.this);
				} catch (final IOException ioe) {
					connected = false;
					socket.close();
					receiver.receivedMessage(null, UDPChannel.this);
					return;
				} catch (final Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

}
