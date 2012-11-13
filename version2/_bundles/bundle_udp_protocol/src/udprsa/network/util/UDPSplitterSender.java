package udprsa.network.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import udprsa.network.MixedChannel;

public class UDPSplitterSender {

	private DatagramSocket udpSocket;
	private int id;
	private InetAddress ip;
	private HashMap<Integer, Long> sendTimings;
	public static final int SIZE = 560;
	public static final int FULLSIZE = 569;

	private ReentrantLock lock = new ReentrantLock(true);
	private MixedChannel channel;
	private HashMap<Integer, HashMap<Integer, DatagramPacket>> sendBuffer;

	// private Random rand;

	public UDPSplitterSender(DatagramSocket udpSocket, InetAddress ip,
			MixedChannel channel) {
		this.udpSocket = udpSocket;
		id = 0;
		this.ip = ip;
		this.sendTimings = new HashMap<Integer, Long>();
		this.sendBuffer = new HashMap<Integer, HashMap<Integer, DatagramPacket>>();
		this.channel = channel;
		if (channel.isRetransEnabled()) {
			Thread t = new ResendThread();
			t.start();
		}
		// test purpose
		// rand = new Random();
	}

	public void splitAndSend(byte[] array) throws IOException {
		int roundingCorrection = 1;
		if (array.length % SIZE == 0) {
			// no correction for rounding down needed
			roundingCorrection = 0;
		}
		// copy for if other tread adapts
		int sendID = id;
		id++;
		if (id == Integer.MAX_VALUE - 10) {
			id = 0;
		}
		sendBuffer.put(sendID, new HashMap<Integer, DatagramPacket>());
		HashMap<Integer, DatagramPacket> packetBuffer = sendBuffer.get(sendID);
		for (int i = 0; i < (array.length / SIZE) + roundingCorrection; i++) {
			byte[] idArray = ByteBuffer.allocate(4).putInt(sendID).array();
			byte[] volgnrArray = ByteBuffer.allocate(4).putInt(i).array();
			byte[] endArray = new byte[] { (byte) (i == (array.length / SIZE)
					+ roundingCorrection - 1 ? 1 : 0) };
			byte[] sending = new byte[FULLSIZE];
			System.arraycopy(idArray, 0, sending, 0, 4);
			System.arraycopy(volgnrArray, 0, sending, 4, 4);
			System.arraycopy(endArray, 0, sending, 8, 1);
			int length = Math.min(SIZE, array.length - (i * SIZE));
			System.arraycopy(array, i * SIZE, sending, 9, length);
			// test purpose

			DatagramPacket packet = new DatagramPacket(sending, sending.length,
					ip, udpSocket.getLocalPort());
			// if (rand.nextInt(100) > 3) {
		//	if (i % 500 != 0) {
				udpSocket.send(packet);
			//} else {
			//	System.out.println("Send failed on test purpose for " + i);
			//}
			if (channel.isRetransEnabled()) {
				packetBuffer.put(i, packet);
			}
		}
		// only put timing if ready (when not put, no resending will occure
		sendTimings.put(sendID, System.currentTimeMillis());
	}

	public void PacketReceived(int id, int packet) {
		lock.lock();
		if (sendBuffer.get(id) != null) {
			//System.out.println("ack for id " + id + " and volgnr " + packet);
			sendBuffer.get(id).remove(packet);
		}
		lock.unlock();
	}

	class ResendThread extends Thread {

		@Override
		public void run() {
			while (channel.isConnected() && channel.isRetransEnabled()) {
				ArrayList<Integer> toRemove = new ArrayList<Integer>();
				// lock.lock();
				for (Integer id : sendTimings.keySet()) {
					System.out.println("check resending for id " + id);
					if (System.currentTimeMillis() - sendTimings.get(id) > 4000) {
						lock.lock();
						HashMap<Integer, DatagramPacket> packetBuffer = (HashMap<Integer, DatagramPacket>) sendBuffer
								.get(id).clone();
						lock.unlock();
						System.out.println("packet buffer for id " + id
								+ " is size " + packetBuffer.size());

						for (DatagramPacket packet : packetBuffer.values()) {
							if (packet != null) {
								try {
									udpSocket.send(packet);
									/*
									 * System.out.println("resend for" + id +
									 * " end volgnr =" + key + "in run " + run);
									 */
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						if (packetBuffer.isEmpty()) {
							toRemove.add(id);
						}
					}
					if (System.currentTimeMillis() - sendTimings.get(id) > 40000) {
						// something fuckd up realy hard, just ignore it
						// (probably lost connection)
						toRemove.add(id);
					}
				}
				lock.lock();
				for (Integer id : toRemove) {
					sendBuffer.remove(id);
					sendTimings.remove(id);
				}
				lock.unlock();

				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					System.out.println("resend sleep interupt" + e.toString());
					e.printStackTrace();
				}
			}
		}
	}
}
