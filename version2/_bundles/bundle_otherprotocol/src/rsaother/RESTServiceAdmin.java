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

import rsaother.exception.RESTException;

/*
 * Main class ... implements RemoteServiceAdmin
 */
public class RESTServiceAdmin implements RemoteServiceAdmin {
	
	BundleContext context;
	
	public RESTServiceAdmin(BundleContext context){
		this.context = context;
	}
	
	public void activate() throws RESTException {
		// -- do something here...
		System.out.println("Hello there!");
		
		
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
		System.out.println("We have a new endpoint!!! ID: "+reference.getProperty("service.id"));
		
		
		// NOTE: return an object of the class RESTExportReference?
		return null;
	}

	@Override
	public ImportRegistration importService(EndpointDescription endpoint) {
		// Maak een importRegistration aan voor de service...
		// HMMM... ?
		System.out.println("Going to import a service!!! ID: "+endpoint.getServiceId());
		
		
		// NOTE: return an object of the class RESTImportReference?
		return null;
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
