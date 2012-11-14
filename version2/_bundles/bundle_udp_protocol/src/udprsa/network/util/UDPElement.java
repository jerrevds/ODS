package udprsa.network.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * udp element to keep in receiver. this will concat all the udp elements from the same stream
 * @author jerrevds
 *
 */
public class UDPElement {
	

	private HashMap<Integer, byte[]> packets;
	private boolean old;
	private int count;
	private UDPReceiver receiver;
	private boolean isPushing;
	private boolean isPushed;

	/**
	 * simple constructor, receiver needed for push
	 * @param receiver
	 */
	public UDPElement(UDPReceiver receiver){
		packets = new HashMap<Integer, byte[]>();
		old = false;
		count = 0;
		this.receiver=receiver;
	}
	
	/**
	 * push a packet to this stream
	 * @param i volgnummer in the stream
	 * @param data the data
	 * @param isLast is it the last packet? (needed to count and push)
	 */
	public void addPacket(int i, byte[] data, boolean isLast){
		if(isLast){
			count = i+1;
		}
		packets.put(i, data);
		old = false;
		System.out.println("check for psuh=" + count + " is  size?" + packets.size() );
		
		if(count !=0 && packets.size() == count && !isPushed){
			//last packet is received and we havn't pushed our object yet, push it (ispushed needed because of multithreading and resending where double resends could occure
			isPushing=true;
			receiver.pushReady(getAsArray());
			isPushed=true;
		}
	}

	/**
	 * get all the packets as one array in correct order
	 * @return
	 */
	private byte[] getAsArray() {
		byte[] array = new byte[count*UDPSplitterSender.SIZE];
		Set<Integer> ids = packets.keySet();
		ArrayList<Integer> list = new ArrayList<Integer>(ids);
		Collections.sort(list);
		for(Integer id: list){
			System.arraycopy(packets.get(id), 0, array, id*UDPSplitterSender.SIZE, UDPSplitterSender.SIZE);
		}
		return array;
	}
/**
 * is marked as old? (ready to remove)
 * @return
 */
	public boolean isOld() {
		return old;
	}
/**
 * set old status for GC
 * @param old
 */
	public void setOld(boolean old) {
		this.old = old;
	}
/**
 * is busy with pushing?
 * @return
 */
	public boolean isPushing() {
		return isPushing;
	}

	public void setPushing(boolean isPushing) {
		this.isPushing = isPushing;
	}
/**
 * is packet pushed?
 * @return
 */
	public boolean isPushed() {
		return isPushed;
	}

	public void setPushed(boolean isPushed) {
		this.isPushed = isPushed;
	}
	
	
	
}
