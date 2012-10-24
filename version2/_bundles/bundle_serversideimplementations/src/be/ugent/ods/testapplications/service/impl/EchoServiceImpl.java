package be.ugent.ods.testapplications.service.impl;

import be.ugent.ods.testapplications.service.interfaces.EchoService;

public class EchoServiceImpl implements EchoService{

	public String echoString(String data) {
		System.out.println("Printing: "+data);
		return "I've printed the data: \"" + data + "\"";
	}

}
