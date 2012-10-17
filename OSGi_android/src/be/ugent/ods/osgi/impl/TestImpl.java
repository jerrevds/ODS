package be.ugent.ods.osgi.impl;

import odscommon.service.Test;

public class TestImpl implements Test{

	public String echoLocation(String name) {
		return "local " + name;
	}

}
