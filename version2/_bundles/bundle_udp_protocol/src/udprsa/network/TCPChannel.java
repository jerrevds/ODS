package udprsa.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import udprsa.network.api.MessageReceiver;
import udprsa.network.api.NetworkChannel;
import udprsa.network.message.ROSGiMessage;

/*
 * TCP implementation of the protocol, sends and recieves ROSGiMessages
 */
public class TCPChannel implements NetworkChannel {

	private Socket socket;
	private ObjectInputStream input;
	private ObjectOutputStream output;

	private MessageReceiver receiver;
	
	boolean connected = true;

	
	public TCPChannel(final Socket socket, MessageReceiver receiver) throws IOException {
		this.receiver = receiver;
		open(socket);
		new ReceiverThread().start();
	}
	
	TCPChannel(String ip, int port, MessageReceiver receiver) throws IOException {
		this(new Socket(ip, port), receiver);
	}

	private void open(final Socket s) throws IOException {
		socket = s;
		try {
			socket.setKeepAlive(true);
		} catch (final Throwable t) {
			// for 1.2 VMs that do not support the setKeepAlive
		}
		socket.setTcpNoDelay(true);
		// Use ObjectOutputstream for object serialization
		// Maybe change to a more efficient serialization algorithm?
		output = new ObjectOutputStream(new BufferedOutputStream(
				socket.getOutputStream()));
		output.flush();
		input = new ObjectInputStream(new BufferedInputStream(socket
				.getInputStream()));
	}



	public void close(){
		try {
			socket.close();
		} catch(IOException ioe){

		}
		// receiver.interrupt();
		connected = false;
	}

	
	public void sendMessage(final ROSGiMessage message)
			throws IOException {
		message.send(output);
	}


	class ReceiverThread extends Thread {
		ReceiverThread() {
			setDaemon(true);
		}

		public void run() {
			while (connected) {
				try {
					final ROSGiMessage msg = ROSGiMessage.parse(input);
					receiver.receivedMessage(msg, TCPChannel.this);
				} catch (final IOException ioe) {
					connected = false;
					try {
						socket.close();
					} catch (final IOException e1) {
					}
					receiver.receivedMessage(null, TCPChannel.this);
					return;
				} catch (final Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}


	@Override
	public String getRemoteAddress() {
		return socket.getInetAddress().getHostAddress()+":"+socket.getPort();
	}

	@Override
	public String getLocalAddress(){
		return socket.getLocalAddress().getHostAddress()+":"+socket.getLocalPort();
	}
}
