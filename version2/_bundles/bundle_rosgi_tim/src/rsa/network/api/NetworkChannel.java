package rsa.network.api;

import java.io.IOException;

import rsa.network.message.ROSGiMessage;

public interface NetworkChannel {

	String getRemoteAddress();
	
	String getLocalAddress();
	
	void sendMessage(final ROSGiMessage message) throws IOException;
	
	void close();
}
