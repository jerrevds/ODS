package ods.client;

import java.util.Hashtable;

import odscommon.service.interfaces.EchoService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {
	private ServiceTracker st;

	public void start(final BundleContext bc) throws Exception {
		System.out.println("start-------------------------------------------");
		st = new ServiceTracker(bc, EchoService.class.getName(), null) {
			@Override
			public Object addingService(ServiceReference reference) {
				Object svc = bc.getService(reference);
				System.out.println("foundref-------------------------------------------");
				if (svc instanceof EchoService) {
					printServiceInfo((EchoService) svc);
					System.out.println("readd-------------------------------------------");
				//we have our reference
				st.close();
				//only add after close to avoind duplication
				Hashtable<String, String> props = new Hashtable<String, String>();
				props.put("type", "testdosgi");
				bc.registerService(
						EchoService.class.getName(), svc, props);
				}
				test(bc);
				return super.addingService(reference);
			}

			

			
		};
		st.open();
	}
	

	public void stop(BundleContext bc) throws Exception {
		st.close();
	}
	private void printServiceInfo(EchoService svc) {
		System.out.println(svc.echoLocation("testertst"));
		
	}
	
	private void test(BundleContext bc) {
		// TODO Auto-generated method stub
		ServiceReference[] refs = null;
		try {
			refs = bc.getServiceReferences(
					EchoService.class.getName(), "(type=testdosgi)");
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EchoService test = (EchoService)bc.getService(refs[0]);
		printServiceInfo(test);
	}
}