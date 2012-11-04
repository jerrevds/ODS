package rsaother;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;

import rsaother.exception.RESTException;

public class Activator implements BundleActivator {
	
	RESTServiceAdmin rsa = null;
	
	@Override
	public void start(BundleContext context) throws Exception {
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("remote.configs.supported", new String[]{"r-osgi-other"});// the properties for the service to create...
		
		try {
			rsa = new RESTServiceAdmin(context);
			rsa.activate();
			context.registerService(RemoteServiceAdmin.class.getName(),rsa, props);
			
			RESTBundleListener listener = new RESTBundleListener(rsa);
			context.addBundleListener(listener);
		} catch(RESTException roe){
			System.out.println("Error activating ROSGi Remote Service Admin");
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		
	}

}
