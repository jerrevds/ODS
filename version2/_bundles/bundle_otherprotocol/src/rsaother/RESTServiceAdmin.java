package rsaother;

import java.util.Collection;
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
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

import rsaother.exception.RESTException;

/*
 * Main class ... implements RemoteServiceAdmin
 */
public class RESTServiceAdmin implements RemoteServiceAdmin {
	
	BundleContext context;
	
	private Component component;
	
	public RESTServiceAdmin(BundleContext context){
		this.context = context;
	}
	
	public void activate() throws RESTException {
		/*
		 * server opstarten
		 */		
		component = new Component();
		component.getServers().add(Protocol.HTTP);
		
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
		component.getDefaultHost().attach("/" + reference.getProperty("service.id") + "/", new Restlet() {
			
		});
		
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
