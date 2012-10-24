package greeter.impl;

import greeter.api.GreeterInterface;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		GreeterInterface greeter = new GreeterImplementation();
		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put("service.exported.interfaces", "*");
		context.registerService(GreeterInterface.class.getName(), greeter, properties);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	
	}

}
