package odscommon.service.impl;

import odscommon.service.interfaces.EchoService;

public class EchoServiceImpl implements EchoService{

	private String location;
	public EchoServiceImpl(String location){
		this.location=location;
	}
	
	public EchoServiceImpl(){
		this.location="not defined!!";
	}
	public String echoLocation(String name) {
		return location + " echoes:"  + name;
	}

}
