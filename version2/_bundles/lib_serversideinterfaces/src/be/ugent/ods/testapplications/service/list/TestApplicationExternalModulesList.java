package be.ugent.ods.testapplications.service.list;

import be.ugent.ods.testapplications.service.interfaces.EchoService;
import be.ugent.ods.testapplications.service.interfaces.GlowFilterService;
import be.ugent.ods.testapplications.service.interfaces.VideoService;

public class TestApplicationExternalModulesList {
	
	// which modules should the server register?
	public static final Class<?>[] modules = {
		EchoService.class, GlowFilterService.class, VideoService.class
	};
}

