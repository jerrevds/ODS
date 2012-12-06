package udprsa.network.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import udprsa.ROSGiServiceAdmin;
import udprsa.network.MixedChannel;
import udprsa.network.message.ROSGiMessage;
import udprsa.network.message.RemoteCallUDPRCVMessage;

public class UDPReceiver {

	private boolean isCleaning;
	private ConcurrentHashMap<Integer, UDPElement> buffer;
	private GarbageCollector cleanThread;
	private MixedChannel channel;
	private HashSet<Integer> resendCheck = new HashSet<Integer>();
	public boolean resend = true;

	public UDPReceiver(MixedChannel channel) {
		isCleaning = false;
		buffer = new ConcurrentHashMap<Integer, UDPElement>();
		this.channel = channel;
		resend = channel.isRetransEnabled();
		if (resend) {
			ResendChecker resendThread = new ResendChecker();
			resendThread.start();
		}
	}

	public void close() {
		isCleaning = false;
		resend = false;
	}

	public void received(byte[] array) {
		ByteBuffer bb = ByteBuffer.wrap(array);
		int id = bb.getInt();
		int volgNr = bb.getInt();
		boolean isLast = array[8] != 0;
		byte[] receiving = new byte[UDPSplitterSender.SIZE];
		System.arraycopy(array, 9, receiving, 0, UDPSplitterSender.SIZE);
		UDPElement element;
		if (buffer.containsKey(id)) {
			element = buffer.get(id);

		} else {
			// ask the resend of a -1 packet so the receiver knows we have got
			// something
			
			element = new UDPElement(this);
		}
		if(volgNr == 0){
			RemoteCallUDPRCVMessage receiveMessage = new RemoteCallUDPRCVMessage(
					ROSGiMessage.NOT_UDP_RECEIVED);
			receiveMessage.setId(id);
			receiveMessage.setVolgnr(-1);
			try {
				channel.sendMessage(receiveMessage);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e.toString());
			}
		}
		resendCheck.add(id);
		element.addPacket(volgNr, receiving, isLast);
		buffer.put(id, element);
		if (!isCleaning) {
			cleanThread = new GarbageCollector();
			cleanThread.start();
			isCleaning = true;

		}

	}

	/**
	 * push byte array of object as ready to channel for proxy handling
	 * 
	 * @param asArray
	 */
	public void pushReady(byte[] asArray) {
		channel.pushReady(asArray);
	}

	/**
	 * clear
	 */
	public void clear() {
		buffer.clear();
	}

	/**
	 * GC thread to clean buffer if packets are located there to loong. buffer
	 * time is the same as the 2* time on which a timeout at sender side would
	 * occure
	 * 
	 * @author jerrevds
	 * 
	 */
	class GarbageCollector extends Thread {

		@Override
		public void run() {
			while (buffer.size() != 0 && isCleaning) {
				try {
					Thread.sleep(ROSGiServiceAdmin.TIMEOUT + 1000);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
				// clone to avoid fucking up the keyset in loop
				Set<Integer> keys = new HashSet<Integer>(buffer.keySet());
				for (Integer id : keys) {
					UDPElement element = buffer.get(id);
					if (element.isOld() && !element.isPushing()) {
						// too old, remove, sender is closed already by timeout
						buffer.remove(id);
					} else if (element.isPushed()) {
						// is psuhed, we don't need to keep it here
						buffer.remove(id);
					} else {
						// passed first run, set as old
						element.setOld(true);
					}
				}
				if (!cleanThread.equals(this)) {
					// ow god some weird shit has happened, stop this and let
					// the next thread take care of all work.
					isCleaning = false;
				}
			}
			isCleaning = false;
		}
	}

	class ResendChecker extends Thread {

		@Override
		public void run() {
			while (resend) {
				//System.out.println("resend check");
				// first check elements where the last element is present but
				// which are not complete and add them to the list
				for (Integer id : buffer.keySet()) {
					if (buffer.get(id).getRSize() < buffer.get(id).getSize()
							|| buffer.get(id).getSize() == 0) {
						resendCheck.add(id);
					}
				}
				// check whcih volgnr are missing
				HashSet<Integer> cloned = (HashSet<Integer>) resendCheck.clone();
				resendCheck.clear();
				for (Integer id : cloned) {
					System.out.println("resend check for id " + id);
					UDPElement element = buffer.get(id);
					if (element != null) {
						int size = Math.max(element.getRSize(),
								element.getSize());
						for (int i = 0; i < size; i++) {
							// we missed something
							if (!element.isVolgPresent(i)) {
								System.out.println("ask resend for " + id + " with volg " + i);
								RemoteCallUDPRCVMessage receiveMessage = new RemoteCallUDPRCVMessage(
										ROSGiMessage.NOT_UDP_RECEIVED);
								receiveMessage.setId(id);
								receiveMessage.setVolgnr(i);
								try {
									channel.sendMessage(receiveMessage);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						// ok, in worst case we've missed some elements at the
						// end,
						// ask to resend elements and this for elements up to 5%
						// in
						// the future
						if (element.getSize() == 0) {
							System.out.println("test resend case 0 with size" + size);
							for (int i = size-1; i < size + (size * ((double)5 / 100)); i++) {
								System.out.println("ask retransmit for" + id + "  and volg" + i);
								RemoteCallUDPRCVMessage receiveMessage = new RemoteCallUDPRCVMessage(
										ROSGiMessage.NOT_UDP_RECEIVED);
								receiveMessage.setId(id);
								receiveMessage.setVolgnr(i);
								try {
									channel.sendMessage(receiveMessage);
								} catch (IOException e) {
									e.printStackTrace();
									throw new RuntimeException(e.toString());
								}
							}
						}
					}
				}

				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
