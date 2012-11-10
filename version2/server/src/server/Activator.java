package server;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.util.tracker.ServiceTracker;

import be.ugent.ods.testapplications.service.list.TestApplicationExternalModulesList;
import be.ugent.ods.testapplications.service.list.TestApplicationProtocolList;

public class Activator implements BundleActivator {
	
	
	//
	// This class Starts the server...
	//
	
	//
	// NOTE: DO NOT MODIFY THE SERVER !!!
	//
	

	public void start(final BundleContext context) throws Exception {
		//
		// export remote modules to all RSA-implementations (!)
		//
		
		
		// EVERYTHING IN THE SERVER HAPPENS AUTOMATICALLY...
		
		/*for(String filter: TestApplicationProtocolList.protocols) {
			registerRSAByFilter(context, filter);
		}*/
		
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {}
				
				System.out.println("");
				System.out.println("");
				System.out.println("[serverinfo]");
				System.out.println("");
				System.out.println("+----------+");
				System.out.println("+ Services |");
				System.out.println("+----------+");
				printAllServices(context);

				System.out.println("");
				System.out.println("+-----------+");
				System.out.println("+ Protocols | ");
				System.out.println("+-----------+");
				printAllRSAs(context);
				System.out.println("");
			}
		}).start();
		
	}
	
	
	
	private void printAllServices(BundleContext context){
		for(Class<?> serverModuleClass: TestApplicationExternalModulesList.modules) {
			final ServiceTracker moduleTracker = new ServiceTracker(context, serverModuleClass, null);
			moduleTracker.open();
			
			try {
				moduleTracker.waitForService(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			ServiceReference<?> exportedservref = moduleTracker.getServiceReference();
			
			if(exportedservref==null) {
				throw new RuntimeException("The following service can not be found..."+serverModuleClass.toString());
			}
			
			System.out.println("Service \""+serverModuleClass.getCanonicalName()+"\" has id: \""+exportedservref.getProperty("service.id")+"\"");
		}
	}
	

	private void printAllRSAs(BundleContext context) {
		int index=0;
		for(String filter: TestApplicationProtocolList.protocols) {
			Filter f;
			try {
				f = context.createFilter("(&("+Constants.OBJECTCLASS+"=org.osgi.service.remoteserviceadmin.RemoteServiceAdmin)"+filter+")");
			} catch (InvalidSyntaxException e) {
				throw new RuntimeException("Filter in illegal format: \""+filter+"\"");
			}
			final ServiceTracker rsaTracker = new ServiceTracker(context, f, null);
			rsaTracker.open();
			
			try {
				rsaTracker.waitForService(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			ServiceReference<?> exportedservref = rsaTracker.getServiceReference();
			
			if(exportedservref==null) {
				throw new RuntimeException("The following RSA can not be found..."+filter);
			}
			
			System.out.println("RSA \""+TestApplicationProtocolList.protocolnames[index]+"\" is loaded!");
			index++;
		}
		
	}
	
	
	private void registerAllServicesToRSA(BundleContext context, RemoteServiceAdmin service){
		for(Class<?> serverModuleClass: TestApplicationExternalModulesList.modules) {
			Map<String, String> props = new HashMap<>();
			
			final ServiceTracker moduleTracker = new ServiceTracker(context, serverModuleClass, null);
			moduleTracker.open();
			
			try {
				moduleTracker.waitForService(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			ServiceReference<?> exportedservref = moduleTracker.getServiceReference();
			
			if(exportedservref==null) {
				throw new RuntimeException("The following service can not be found..."+serverModuleClass.toString());
			}
			
			service.exportService(exportedservref, props);
			System.out.println("The following service is registered \""+serverModuleClass.getCanonicalName()+"\" on the RSA-implementation: \""+service.getClass().getCanonicalName()+"\"");
		}
	}
	
	
	
	private void registerRSAByFilter(final BundleContext context, final String filter) {
		
		//1: get the required service
		Filter f;
		try {
			f = context.createFilter("(&("+Constants.OBJECTCLASS+"=org.osgi.service.remoteserviceadmin.RemoteServiceAdmin)"+filter+")");
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException("Filter in illegal format: \""+filter+"\"");
		}
		final ServiceTracker rsaTracker = new ServiceTracker(context, f, null);
		rsaTracker.open();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					rsaTracker.waitForService(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
				// 2: get the RemoteServiceAdmin itself
				RemoteServiceAdmin rsa = (RemoteServiceAdmin)rsaTracker.getService();
				
				if(rsa!=null){
					//3: register all services to this RSA
					registerAllServicesToRSA(context, rsa);
				}else{
					throw new RuntimeException("Not found: \""+filter+"\"");
				}
				
				rsaTracker.close();
				
			}
		}).start();
		
	}

	
	public void stop(BundleContext context) throws Exception {
	}
}