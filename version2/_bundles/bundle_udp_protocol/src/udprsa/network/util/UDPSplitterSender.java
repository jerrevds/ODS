package udprsa.network.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import udprsa.ROSGiServiceAdmin;
import udprsa.network.MixedChannel;

public class UDPSplitterSender {

	private DatagramSocket udpSocket;
	private int id;
	private InetAddress ip;
	private ConcurrentHashMap<Integer, Long> sendTimings;
	public static final int SIZE = 560;
	public static final int FULLSIZE = 569;

	private MixedChannel channel;
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, DatagramPacket>> sendBuffer;

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
	 * sends an udp stream. If the array is to long, the array will be split up
	 * in multiple packets
	 * 
	 * @param array
	 *            byte array to send
	 * @throws IOException
	 */
	public void splitAndSend(byte[] array) throws IOException {
		int roundingCorrection = 1;
		System.out.println("end size is:" + array.length);
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
		if (channel.isRetransEnabled()) {
			// if we won't lock, we get ack's before they are added (due
			// rehashes etc), so lock it up to avoid unnec. resends
			sendBuffer.put(sendID,
					new ConcurrentHashMap<Integer, DatagramPacket>());
		}
		for (int i = 0; i < (array.length / SIZE) + roundingCorrection; i++) {
			byte[] idArray = ByteBuffer.allocate(4).putInt(sendID).array();
			byte[] volgnrArray = ByteBuffer.allocate(4).putInt(i).array();
			byte[] endArray = new byte[] { (byte) (i == (array.length / SIZE)
					+ roundingCorrection - 1 ? 1 : 0) };
			int length = Math.min(SIZE, array.length - (i * SIZE));
			byte[] sending;
			if (length == SIZE) {
				//System.out.println("udp max sent");
				sending = new byte[FULLSIZE];
			} else {
				System.out.println("udp smaller, only send "
						+ ((FULLSIZE - SIZE) + length));
				sending = new byte[(FULLSIZE - SIZE) + length];
			}

			// adapt size if is smaller
			System.arraycopy(idArray, 0, sending, 0, 4);
			System.arraycopy(volgnrArray, 0, sending, 4, 4);
			System.arraycopy(endArray, 0, sending, 8, 1);

			System.arraycopy(array, i * SIZE, sending, 9, length);
			DatagramPacket packet = new DatagramPacket(sending, sending.length,
					ip, udpSocket.getLocalPort());
			if (channel.isRetransEnabled()) {
				// store before send to avoid weird shizle that we have an ack
				// before it is in the map
				sendBuffer.get(sendID).put(i, packet);
			}
			// test
		//	Random rand = new Random();
			//if (i!=0) {
				udpSocket.send(packet);
			//}
			//else{
			//	System.out.println("lost:" + i);
			//}

		}

		// only put timing if ready (when not put, no resending will occure
		sendTimings.put(sendID, System.currentTimeMillis());

	}

	/**
	 * class responsibel to create a minor form of retransmission
	 * 
	 * @author jerrevds
	 * 
	 */
	class ResendThread extends Thread {

		@Override
		public void run() {
			while (channel.isConnected() && channel.isRetransEnabled()) {
				ArrayList<Integer> toRemove = new ArrayList<Integer>();
				// check if send occured long enough ago (give time to receive
				// ack)
				for (Integer id : sendTimings.keySet()) {
					// System.out.println("check resending for id " + id);
					if (System.currentTimeMillis() - sendTimings.get(id) > ROSGiServiceAdmin.TIMEOUT) {
						// cleanup
						toRemove.add(id);
					}
					if (System.currentTimeMillis() - sendTimings.get(id) > 750
							&& sendBuffer.get(id).get(0) != null) {
						// still no ack, womething is probably fucked up or the
						// stream is only one packet big (resend)
						System.out.println("udp: auto resend:" + id);
						try {
							// resend can ask to much

							udpSocket.send((DatagramPacket) sendBuffer.get(id)
									.get(0));

						} catch (IOException e) {

							e.printStackTrace();
						}
					}
				}

				// remove timings of sent buffers
				for (Integer id : toRemove) {
					sendBuffer.remove(id);
					sendTimings.remove(id);
				}
				try {
					// just sleep so that new ack's could come in
					Thread.sleep(500);
				} catch (InterruptedException e) {
					System.out.println("resend sleep interupt" + e.toString());
					e.printStackTrace();
				}
			}
		}
	}

	public void resend(int id, int volgnr) {
		System.out.println("udp: resend:" + id + " volgnr " + volgnr);
		if(volgnr == -1){
			sendBuffer.get(id).remove(0);
		}
		try {
			// resend can ask to much
			if (sendBuffer.get(id).get(volgnr) != null) {
				udpSocket.send((DatagramPacket) sendBuffer.get(id).get(volgnr));
			}
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

}
