package udprsa.network.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import udprsa.ROSGiServiceAdmin;
import udprsa.network.MixedChannel;

public class UDPSplitterSender {

	private DatagramSocket udpSocket;
	private int id;
	private InetAddress ip;
	private ConcurrentHashMap<Integer, Long> sendTimings;
	public static final int SIZE = 560;
	public static final int FULLSIZE = 569;
	private Semaphore semaphore = new Semaphore(1);
	private MixedChannel channel;
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, DatagramPacket>> sendBuffer;

	// private Random rand;

	public UDPSplitterSender(DatagramSocket udpSocket, InetAddress ip,
			MixedChannel channel) {
		this.udpSocket = udpSocket;
		id = 0;
		this.ip = ip;
		this.sendTimings = new ConcurrentHashMap<Integer, Long>();
		this.sendBuffer = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, DatagramPacket>>();
		this.channel = channel;
		if (channel.isRetransEnabled()) {
			Thread t = new ResendThread();
			t.start();
		}
		// test purpose
		// rand = new Random();
	}

	/**
	 * sends an udp stream. If the array is to long, the array will be split up in multiple packets
	 * @param array byte array to send
	 * @throws IOException
	 */
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
		sendBuffer.put(sendID, new ConcurrentHashMap<Integer, DatagramPacket>());
		ConcurrentHashMap<Integer, DatagramPacket> packetBuffer = sendBuffer.get(sendID);
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
			udpSocket.send(packet);
			if (channel.isRetransEnabled()) {
				lock();
				packetBuffer.put(i, packet);
				unlock();
			}
		}
		// only put timing if ready (when not put, no resending will occure
		sendTimings.put(sendID, System.currentTimeMillis());
	}

/**
 * ack method for packets
 * @param id the id of the udp stream
 * @param packet the following number of a packet in the udp stream
 * this will remove the packet from te buffer
 */
	public void PacketReceived(int id, int packet) {
		lock();
		if (sendBuffer.get(id) != null) {
			sendBuffer.get(id).remove(packet);
		}
		unlock();
	}

	
	/**
	 * class responsibel to create a minor form of retransmission
	 * @author jerrevds
	 *
	 */
	class ResendThread extends Thread {

		@Override
		public void run() {
			while (channel.isConnected() && channel.isRetransEnabled()) {
				ArrayList<Integer> toRemove = new ArrayList<Integer>();
				//check if send occured long enough ago (give time to receive ack)
				for (Integer id : sendTimings.keySet()) {
					System.out.println("check resending for id " + id);
					if (System.currentTimeMillis() - sendTimings.get(id) > 4000) {
						System.out.println("packet buffer for id " + id + " is size " + sendBuffer.get(id).size());
						//for all packets (if packet = ack's, it is removed
						lock();
						for (DatagramPacket packet : sendBuffer.get(id).values()) {
							if (packet != null) {
								try {
									//resend
									udpSocket.send(packet);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						unlock();
						//remove empty buffers (all is send)
						if (sendBuffer.get(id).isEmpty()) {
							toRemove.add(id);
						}
					}
					if (System.currentTimeMillis() - sendTimings.get(id) > ROSGiServiceAdmin.TIMEOUT) {
						// something fuckd up realy hard, just ignore it
						// (probably lost connection)
						toRemove.add(id);
					}
				}

				//remove timings of sent buffers
				for (Integer id : toRemove) {
					lock();
					sendBuffer.remove(id);
					sendTimings.remove(id);
					unlock();
				}


				try {
					//just sleep so that new ack's could come in
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					System.out.println("resend sleep interupt" + e.toString());
					e.printStackTrace();
				}
			}
		}
	}
	
	public void lock(){
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void unlock(){
		semaphore.release();
	}
}
