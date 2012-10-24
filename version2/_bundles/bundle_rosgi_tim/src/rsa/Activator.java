package rsa;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import rsa.command.RSACommand;
import rsa.exception.ROSGiException;

public class Activator implements BundleActivator {

	ROSGiServiceAdmin rsa = null;
	
	@Override
	public void start(BundleContext context) throws Exception {
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("remote.configs.supported", new String[]{"r-osgi"});
		
		try {
			rsa = new ROSGiServiceAdmin(context);
			rsa.activate();
			context.registerService(org.osgi.service.remoteserviceadmin.RemoteServiceAdmin.class.getName(),rsa, props);
			
			ROSGiBundleListener listener = new ROSGiBundleListener(rsa);
			context.addBundleListener(listener);
		} catch(ROSGiException roe){
			System.out.println("Error activating ROSGi Remote Service Admin");
		}
		
		// some shell commands for debugging
		try {
			context.getBundle().loadClass("org.apache.felix.shell.Command"); // dirty check to see if shell bundle is deployed...
			context.registerService(org.apache.felix.shell.Command.class.getName(),new RSACommand(context), props);
		} catch(Exception e){}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		rsa.deactivate();
	}

}
