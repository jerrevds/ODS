package be.ugent.ods.testapplications.service;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import be.ugent.ods.testapplications.service.impl.EchoServiceImpl;
import be.ugent.ods.testapplications.service.impl.GlowFilterImpl;
import be.ugent.ods.testapplications.service.interfaces.EchoService;
import be.ugent.ods.testapplications.service.interfaces.GlowFilterService;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("service.exported.interfaces", "*");

		// -- ECHOSERVICE --

		// make a service
		EchoServiceImpl impl = new EchoServiceImpl();

		// register the service
		context.registerService(EchoService.class.getName(), impl, props);

		// -- SOME OTHER SERVICE --
		// make a service
		props = new Hashtable<String, Object>();
		props.put("service.exported.interfaces", "*");
		GlowFilterImpl glowImpl = new GlowFilterImpl();

		// register the service
		context.registerService(GlowFilterService.class.getName(), glowImpl, props);

		// -- SOME OTHER SERVICE --

		// ...
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
