package ods.client;

import java.io.IOException;
import java.util.Hashtable;

import odscommon.service.interfaces.EchoService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.ethz.iks.r_osgi.RemoteOSGiException;
import ch.ethz.iks.r_osgi.RemoteOSGiService;
import ch.ethz.iks.r_osgi.RemoteServiceReference;
import ch.ethz.iks.r_osgi.URI;

public class Activator implements BundleActivator {
	private ServiceTracker st;

	public void start(final BundleContext bc) throws Exception {
		System.out.println(" rosgi start-------------------------------------------");
		ServiceReference<?> serviceRef = bc.getServiceReference(
				RemoteOSGiService.class.getName());
		if (serviceRef == null) {
			System.err.println("rosgi service not found");
		} else {
			final RemoteOSGiService remote = (RemoteOSGiService) bc.getService(serviceRef);
			try {
				remote.connect(new URI("r-osgi://192.168.0.128:9278"));
			} catch (RemoteOSGiException e) {
				System.err.println("rosgi fout: " + e.toString());
			} catch (IOException e) {
				System.err.println("rosgi io fout: " + e.toString());
			}

			final RemoteServiceReference[] references = remote
					.getRemoteServiceReferences(new URI(
							"r-osgi://192.168.0.128:9278"),
							EchoService.class.getName(), null);
			if (references == null) {
				System.err.println("rosgi fout: " +"service not found");
			} else {
			
				// eigen toevoeging final GlowFilterService
				EchoService rosgitest = (EchoService) remote
						.getRemoteService(references[0]);
				printServiceInfo(rosgitest);
				Hashtable<String, String> props = new Hashtable<String, String>();
				props.put("type", "testrsosgi");
				bc.registerService(
						EchoService.class.getName(), rosgitest, props);
				test(bc);
			}
		}
	}
	

	public void stop(BundleContext bc) throws Exception {
		st.close();
	}
	private void printServiceInfo(EchoService svc) {
		System.out.println(svc.echoLocation("test for rosgi"));
		
	}
	
	private void test(BundleContext bc) {
		// TODO Auto-generated method stub
		ServiceReference[] refs = null;
		try {
			refs = bc.getServiceReferences(
					EchoService.class.getName(), "(type=testrosgi)");
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i = 0; i<refs.length;i++){
			EchoService test = (EchoService)bc.getService(refs[i]);
			printServiceInfo(test);
		}

	}
}