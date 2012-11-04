package rsaother;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ExportReference;

/*
 * Implementation of the R-OSGi endpoint
 * 
 * Keeps a map of Method objects hashed by signature to which method calls are dispatched
 * 
 * Also keeps a list of aqcuired references to this endpoint
 */
public class RESTExportReference implements ExportReference {

	private ServiceReference serviceReference;

	public RESTExportReference(BundleContext context, ServiceReference serviceReference){
		this.serviceReference = serviceReference;
	}
	
	@Override
	public ServiceReference getExportedService() {
		return serviceReference;
	}

	@Override
	public EndpointDescription getExportedEndpoint() {
		throw new RuntimeException("Sorry, not supported :)   - Jeroen P. ");
		/*
		// always re-fetch the address in order to mitigate runtime ip change
		String endpointId = "r-osgi://"+factory.getAddress()+"#"+serviceId;
		
		endpointDescriptionProperties.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
				endpointId);
		
		return new EndpointDescription(endpointDescriptionProperties);*/
	}
}
