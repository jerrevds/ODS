package rsaother;

import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ExportReference;
import org.osgi.service.remoteserviceadmin.ExportRegistration;
import org.osgi.service.remoteserviceadmin.ImportReference;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.restlet.Server;
import org.restlet.data.Protocol;

import rsaother.exception.RESTException;

/*
 * Main class ... implements RemoteServiceAdmin
 */
public class RESTServiceAdmin implements RemoteServiceAdmin {
	
	BundleContext context;
	
	private Server server;
	
	public RESTServiceAdmin(BundleContext context){
		this.context = context;
	}
	
	public void activate() throws RESTException {
		/*
		 * server opstarten
		 */
		String networkInterface = context.getProperty("rsa.interface");
		// first check if it exists
		try {
			boolean exists = false;
			if(networkInterface!=null){	
				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
		            NetworkInterface intf = en.nextElement();
		            if(intf.getName().equals(networkInterface)){
		            	if(intf.isUp())
		            		exists = true;	
		            }
		        }
				
			}
		}catch(Exception e){}
		
		int port = -1;
		String portString = context.getProperty("rsa.port");
		if(portString!=null){
			port = Integer.parseInt(portString);
		}
		
		server = new Server(Protocol.HTTP, port);
		
		/*
		 * ServiceListener that automatically exports services with exported interfaces
		 * as defined by the Remote Services specification
		 */
		try {
			ServiceTracker serviceTracker = new ServiceTracker(context,
				context.createFilter("(service.exported.interfaces=*)"), 
				new ServiceTrackerCustomizer() {

					@Override
					public Object addingService(ServiceReference ref) {
						Collection<ExportRegistration> regs = exportService(ref, null);
						return regs;
					}

					@Override
					public void modifiedService(ServiceReference ref,
							Object regs) {}

					@Override
					public void removedService(ServiceReference ref,
							Object regs) {
						for(ExportRegistration r : (Collection<ExportRegistration>) regs){
							r.close();
						}
					}
				});
			serviceTracker.open();
		}catch(InvalidSyntaxException e){}
	}

	
	
	@Override
	public Collection<ExportRegistration> exportService(ServiceReference reference, Map<String, ?> properties) {
		//Kortom:
		// - we hebben een implementatie (in reference) van een service
		// - de interface moeten we wel aangeraken (kan in properties)
		// - die moeten we toevoegen aan de Restlet server...
		
		
		return null;// Sorry, not supported -Jeroen
	}

	@Override
	public ImportRegistration importService(EndpointDescription endpoint) {
		Class<?> interfaceClass = (Class<?>) endpoint.getProperties().get("interface");
		String baseUrl = endpoint.getId();
		
		RESTImportProxyHandler proxyhandler = new RESTImportProxyHandler(interfaceClass, baseUrl);
		
		return proxyhandler.getImportRegistration(context, endpoint);
	}
	
	
	

	@Override
	public Collection<ExportReference> getExportedServices() {
		throw new RuntimeException("Sorry, not supported yet... -Jeroen");
	}

	@Override
	public Collection<ImportReference> getImportedEndpoints() {
		throw new RuntimeException("Sorry, not supported yet... -Jeroen");
	}
	
}
