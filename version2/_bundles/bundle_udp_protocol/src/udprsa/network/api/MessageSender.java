package udprsa.network.api;

import udprsa.exception.ROSGiException;
import udprsa.network.message.ROSGiMessage;

public interface MessageSender {

	public void sendMessage(ROSGiMessage msg, NetworkChannel channel) throws ROSGiException;
	
	public ROSGiMessage sendAndWaitMessage(ROSGiMessage msg, NetworkChannel channel) throws ROSGiException;
}
