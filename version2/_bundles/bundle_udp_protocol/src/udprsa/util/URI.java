package udprsa.util;

/*
 * Utility class for parsing r-osgi uris
 */
public class URI {

	private String protocol;
	private String ip;
	private int port;
	private String serviceId;
	
	public URI(final String uri){
		parse(uri);
	}

	private void parse(final String uriString) {
		try {
			int cs = 0;
			int ce = uriString.length();
			final int p1 = uriString.indexOf("://"); 
			if (p1 > -1) {
				protocol = uriString.substring(0, p1);
				cs = p1 + 3;
			} else {
				protocol = "r-osgi"; 
			}
			final int p2 = uriString.lastIndexOf("#"); 
			if (p2 > -1) {
				serviceId = uriString.substring(p2 + 1);
				ce = p2;
			}
			final int p3 = uriString.indexOf(":", cs);
			if (p3 > -1) {
				port = Integer.parseInt(uriString.substring(p3 + 1, ce));
				ce = p3;
			} else {
				if ("r-osgi".equals(protocol)) { 
					// FIXME: this should be the actual port of this instance
					// !?!
					port = 9278;
				} else if ("http".equals(protocol)) {
					port = 80;
				} else if ("https".equals(protocol)) { 
					port = 443;
				}
			}
			ip = uriString.substring(cs, ce);
		} catch (final IndexOutOfBoundsException i) {
			throw new IllegalArgumentException(uriString + " caused " //$NON-NLS-1$
					+ i.getMessage());
		}
	}
	
	public String getProtocol(){
		return protocol;
	}
	
	public String getIP(){
		return ip;
	}
	
	public int getPort(){
		return port;
	}
	
	public String getServiceId(){
		return serviceId;
	}
	
	public String getAddress(){
		return ip+":"+port;
	}
	
	public String toString() {
		return protocol + "://" + ip + ":" + port + "#" +serviceId; 
	}

	public boolean equals(final Object other) {
		if (other instanceof String) {
			return equals(new URI((String) other));
		} else if (other instanceof URI) {
			final URI otherURI = (URI) other;
			return protocol.equals(otherURI.protocol)
					&& ip.equals(otherURI.ip)
					&& port == otherURI.port
					&& serviceId == otherURI.serviceId;
		} else {
			return false;
		}
	}
}
