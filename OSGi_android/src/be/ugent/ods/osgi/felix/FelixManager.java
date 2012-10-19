package be.ugent.ods.osgi.felix;

import java.io.File;
import java.util.Hashtable;
import java.util.Properties;

import odscommon.service.impl.EchoServiceImpl;
import odscommon.service.interfaces.GlowFilterService;
import odscommon.service.interfaces.EchoService;

import org.apache.felix.framework.Felix;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;



public class FelixManager {
	private String rootPath;

	private Felix felix;

	private Properties felixProperties;

	private File bundlesDir;
	private File cacheDir;

	private ServiceReference<?> serviceRef;

	public FelixManager(String rootPath) {
		this.rootPath = rootPath;

		felixProperties = new FelixProperties(this.rootPath);
		
		bundlesDir = new File(rootPath + "/felix/bundle");
		if (!bundlesDir.exists()) {
			if (!bundlesDir.mkdirs()) {
				throw new IllegalStateException("Unable to create bundles dir");
			}
		}
		
		cacheDir = new File(rootPath + "/felix/cache");
		if (!cacheDir.exists()) {
			if (!cacheDir.mkdirs()) {
				throw new IllegalStateException(
						"Unable to create felixcache dir");
			}
		}
		
		
		/**
		 * activate local
		 */
		try {
			felix = new Felix(felixProperties);
			felix.start();

	        // Create a property lookup service implementation.
	        EchoService test = new EchoServiceImpl();
	        // Register the property lookup service and save
	        // the service registration.
	        Hashtable<String, String> props = new Hashtable<String, String>();
	        props.put("type", "testlocal");
	       felix.getBundleContext().registerService(
	        		EchoService.class.getName(), test, props);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		

		/* serviceRef = felix.getBundleContext().getServiceReference(RemoteOSGiService.class.getName());
        if (serviceRef == null) {
        	Log.d("ROSGi", "rosgi service not found");
        } else {
            final RemoteOSGiService remote = (RemoteOSGiService) felix.getBundleContext().getService(serviceRef);

                try {
					remote.connect(new URI("r-osgi://localhost:9278"));
				} catch (RemoteOSGiException e) {
					Log.d("ROSGi", "rosgie"+e.toString());
				} catch (IOException e) {
					Log.d("ROSGi", "io"+e.toString());
				}

                final RemoteServiceReference[] references = remote.getRemoteServiceReferences(
                    new URI("r-osgi://localhost:9278"), GlowFilterService.class.getName(), null);
                if (references == null) {
                	Log.d("ROSGi", "service not found");
                } else {
                	//eigen toevoeging
                    final GlowFilterService rglower = (GlowFilterService) remote.getRemoteService(references[0]);
                    Hashtable<String, String> props = new Hashtable<String, String>();
        	        props.put("type", "rglow");
        	       felix.getBundleContext().registerService(
        	        		GlowFilterService.class.getName(), rglower, props);
                   /* System.out.println("All user names:");
                    final List<String> names = userManager.getUserNames();
                    for (final String name : names) {
                        System.out.println("Name = " + name);
                    }
                }
           
        }*/

	  
	}

	public Felix getFelix() {
		return felix;
	}

	public Properties getFelixProperties() {
		return felixProperties;
	}

	public void stopFelix() {
		felix.getBundleContext().ungetService(serviceRef);
		try {
			felix.stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}

	}
}