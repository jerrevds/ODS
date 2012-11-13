package udprsa.network.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class UDPElement {
	

	private HashMap<Integer, byte[]> packets;
	private boolean old;
	private int count;
	private UDPReceiver receiver;
	private boolean isPushing;
	private boolean isPushed;

	public UDPElement(UDPReceiver receiver){
		packets = new HashMap<Integer, byte[]>();
		old = false;
		count = 0;
		this.receiver=receiver;
	}
	
	public void addPacket(int i, byte[] data, boolean isLast){
		if(isLast){
			count = i+1;
		}
		packets.put(i, data);
		old = false;
		System.out.println("check for psuh=" + count + " is  size?" + packets.size() );
		if(count !=0 && packets.size() == count && !isPushed){
			isPushing=true;
			receiver.pushReady(getAsArray());
			isPushed=true;
		}
	}

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

	public boolean isOld() {
		return old;
	}

	public void setOld(boolean old) {
		this.old = old;
	}

	public boolean isPushing() {
		return isPushing;
	}

	public void setPushing(boolean isPushing) {
		this.isPushing = isPushing;
	}

	public boolean isPushed() {
		return isPushed;
	}

	public void setPushed(boolean isPushed) {
		this.isPushed = isPushed;
	}
	
	
	
}
