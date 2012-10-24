package be.ugent.ods.testapplications.service.list;


public class TestApplicationProtocolList {
	
	// which modules should the server register?
	public static final String[] protocols = {
		"(remote.configs.supported=r-osgi)"// 0:  R-OSGi van Tim
	};
	
	public static final int PROTOCOL_LOCAL = -1;// -1: Lokale uitvoering
	public static final int PROTOCOL_ROSGI_TIM = 0;// 0:  R-OSGi van Tim
	
	public static final String[] protocolnames = {
		"R-OSGi Tim"
	};
}
