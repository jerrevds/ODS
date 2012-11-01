package udprsa.network.api;

import udprsa.network.message.ROSGiMessage;

public interface MessageReceiver {

	public void receivedMessage(ROSGiMessage msg, NetworkChannel channel);
}
