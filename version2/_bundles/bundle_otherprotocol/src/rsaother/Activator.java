package rsaother;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	@Override
	public void start(BundleContext context) throws Exception {
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("remote.configs.supported", new String[]{"r-osgi-other"});// the properties for the service to create...
		
		System.out.println("[OTHER PROTOCOL] Hello world from the \"other protocol\"-bundle...");
		System.out.println("[OTHER PROTOCOL] NOTE: No RSA-service is created (will throw an exception on the server)...");
		System.out.println("[OTHER PROTOCOL] NOTE: AND: Androidapp will crash if you press the other button :) ");
		System.out.println("[OTHER PROTOCOL] (But server will just work fine...)");
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		
	}

}
