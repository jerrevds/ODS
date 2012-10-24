package rsa.network.api;

import java.io.IOException;

import rsa.util.URI;

public interface NetworkChannelFactory {

	String getAddress();
	
	NetworkChannel getChannel(URI uri);
	
	void deleteChannel(NetworkChannel channel);
	
	void activate() throws IOException;
	
	void deactivate() throws IOException;
}
