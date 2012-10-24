package be.ugent.ods.testapplications.service.list;

import be.ugent.ods.testapplications.service.interfaces.EchoService;

public class TestApplicationExternalModulesList {
	
	// which modules should the server register?
	public static final Class<?>[] modules = {
		EchoService.class//echo service
	};
}
