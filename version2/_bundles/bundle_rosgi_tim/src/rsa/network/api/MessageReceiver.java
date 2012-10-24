package rsa.network.api;

import rsa.network.message.ROSGiMessage;

public interface MessageReceiver {

	public void receivedMessage(ROSGiMessage msg, NetworkChannel channel);
}
