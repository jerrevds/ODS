package udprsa.network.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import udprsa.ROSGiServiceAdmin;
import udprsa.network.MixedChannel;
import udprsa.network.message.ROSGiMessage;
import udprsa.network.message.RemoteCallUDPRCVMessage;

public class UDPReceiver {

	private boolean isCleaning;
	private HashMap<Integer, UDPElement> buffer;
	private GarbageCollector cleanThread;
	private MixedChannel channel;



	public UDPReceiver(MixedChannel channel) {
		isCleaning=false;
		buffer = new HashMap<Integer, UDPElement>();
		this.channel = channel;
	}
	
	public void received(byte[] array){
		ByteBuffer bb = ByteBuffer.wrap(array);
		int id = bb.getInt();
		int volgNr = bb.getInt();
		boolean isLast = array[8]!=0;
		byte[] receiving = new byte[UDPSplitterSender.SIZE];
		System.arraycopy(array, 9, receiving, 0, UDPSplitterSender.SIZE);
		UDPElement element;
		if(buffer.containsKey(id)){
			element = buffer.get(id);
		}else{
			element = new UDPElement(this);
		}
		element.addPacket(volgNr, receiving, isLast);
		buffer.put(id, element);
		if(!isCleaning){
			cleanThread = new GarbageCollector();
			cleanThread.start();
			isCleaning=true;
			
		}
		RemoteCallUDPRCVMessage receiveMessage = new RemoteCallUDPRCVMessage(ROSGiMessage.UDP_RECEIVED);
		receiveMessage.setId(id);
		receiveMessage.setVolgnr(volgNr);
		try {
			channel.sendMessage(receiveMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void pushReady(byte[] asArray) {
		channel.pushReady(asArray);
	}
	
	public void clear(){
		buffer.clear();
	}

	
	
	class GarbageCollector extends Thread{
		
		@Override
		public void run() {
			while(buffer.size() !=0 && isCleaning){
				try {
					Thread.sleep(ROSGiServiceAdmin.TIMEOUT + 1000);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				//clone to avoid fucking up the keyset in loop
				Set<Integer> keys = new HashSet<Integer>(buffer.keySet());
				for(Integer id : keys){
					UDPElement element = buffer.get(id);
					if(element.isOld() && ! element.isPushing()){
						buffer.remove(id);
					}else if(element.isPushed()){
						buffer.remove(id);
					}else{
						element.setOld(true);
					}
				}
				if(!cleanThread.equals(this)){
					//ow god some weird shit has happened, stop this and let the next thread take care of all work.
					isCleaning = false;
				}
			}
			isCleaning = false;
		}
	}
}
