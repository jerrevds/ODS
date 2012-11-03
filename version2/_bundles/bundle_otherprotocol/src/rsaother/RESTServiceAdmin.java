package rsaother;

import java.util.Collection;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ExportReference;
import org.osgi.service.remoteserviceadmin.ExportRegistration;
import org.osgi.service.remoteserviceadmin.ImportReference;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;

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
	}

	@Override
	public Collection<ExportRegistration> exportService(ServiceReference reference, Map<String, ?> properties) {
		//Kortom:
		// - we hebben een implementatie (in reference) van een service
		// - de interface moeten we wel aangeraken (kan in properties)
		// - die moeten we toevoegen aan de Restlet server...
		
		
		// NOTE: return an object of the class RESTExportReference?
		return null;
	}

	@Override
	public ImportRegistration importService(EndpointDescription endpoint) {
		// Maak een importRegistration aan voor de service...
		// HMMM... ?
		System.out.println("We have a new endpoint!!! ID: "+endpoint.getServiceId());
		
		
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
