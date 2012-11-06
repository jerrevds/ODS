package rsaother;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ImportReference;

/*
 * 
 */
public class RESTImportReference implements ImportReference{

	private ServiceRegistration registration;
	private EndpointDescription endpointDescription;
	
	public RESTImportReference(ServiceRegistration registration, EndpointDescription endpointDescription){
		this.endpointDescription = endpointDescription;
		this.registration = registration;
	}
	
	@Override
	public ServiceReference getImportedService() {
		return registration.getReference();
	}

	@Override
	public EndpointDescription getImportedEndpoint() {
		return endpointDescription;
	}
}
