package be.ugent.ods.testapplications.service.interfaces;

import udprsa.annotation.UDP;

public interface EchoService {
	@UDP
	public String echoString(String name);
}
