package odscommon.service.impl;

import odscommon.service.interfaces.EchoService;

public class EchoServiceImpl implements EchoService{

	public String echoLocation(String name) {
		return "local " + name;
	}

}
