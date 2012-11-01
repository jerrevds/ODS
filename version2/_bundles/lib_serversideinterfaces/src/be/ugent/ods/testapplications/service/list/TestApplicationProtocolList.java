package be.ugent.ods.testapplications.service.list;


public class TestApplicationProtocolList {
	
	// which modules should the server register?
	public static final String[] protocols = {
		"(remote.configs.supported=r-osgi)",
		"(remote.configs.supported=r-osgi-udp)",
		"(remote.configs.supported=r-osgi-other)"
	};
	
	public static final int PROTOCOL_LOCAL = -1;
	public static final int PROTOCOL_ROSGI_TIM = 0;
	public static final int PROTOCOL_ROSGI_UDP = 1;
	public static final int PROTOCOL_OTHER = 2;
	
	
	public static final String[] protocolnames = {
		"R-OSGi Tim", 
		"R-OSGi UDP",
		"Other protocol"
	};
}
