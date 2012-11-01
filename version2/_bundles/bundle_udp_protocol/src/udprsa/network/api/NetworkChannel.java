package udprsa.network.api;

import java.io.IOException;

import udprsa.network.message.ROSGiMessage;

public interface NetworkChannel {

	String getRemoteAddress();
	
	String getLocalAddress();
	
	void sendMessage(final ROSGiMessage message) throws IOException;
	
	void close();
}
