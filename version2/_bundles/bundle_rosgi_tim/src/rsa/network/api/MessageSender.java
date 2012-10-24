package rsa.network.api;

import rsa.exception.ROSGiException;
import rsa.network.message.ROSGiMessage;

public interface MessageSender {

	public void sendMessage(ROSGiMessage msg, NetworkChannel channel) throws ROSGiException;
	
	public ROSGiMessage sendAndWaitMessage(ROSGiMessage msg, NetworkChannel channel) throws ROSGiException;
}
