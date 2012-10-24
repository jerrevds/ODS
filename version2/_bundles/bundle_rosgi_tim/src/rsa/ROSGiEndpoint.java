package rsa;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ExportReference;

import rsa.network.api.NetworkChannelFactory;
import rsa.util.MethodSignature;
import rsa.util.PropertiesUtil;

/*
 * Implementation of the R-OSGi endpoint
 * 
 * Keeps a map of Method objects hashed by signature to which method calls are dispatched
 * 
 * Also keeps a list of aqcuired references to this endpoint
 */
public class ROSGiEndpoint implements ExportReference {

	private String serviceId;
	private Object serviceObject;
	private ServiceReference serviceReference;
	private Map<String, Method> methodList = new HashMap<String, Method>();
	
	private Map<String, Object> endpointDescriptionProperties;
	
	private NetworkChannelFactory factory;
	
	private int refCount = 0;

	public ROSGiEndpoint(BundleContext context, 
			ServiceReference serviceReference, 
			Map<String, ?> overridingProperties,  
			String frameworkId, 
			NetworkChannelFactory factory){
		// keep factory to fetch address for endpoint id
		this.factory = factory;
		
		overridingProperties = PropertiesUtil.mergeProperties(serviceReference, 
				overridingProperties == null ? Collections.EMPTY_MAP : overridingProperties);

		// First get exported interfaces
		String[] exportedInterfaces = PropertiesUtil.getExportedInterfaces(serviceReference, overridingProperties);
		if (exportedInterfaces == null)
			throw new IllegalArgumentException(
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES
							+ " not set"); 
		
		// Check if all exported interfaces are contained in service's OBJECTCLASS
		if (!validExportedInterfaces(serviceReference, exportedInterfaces))
			throw new IllegalArgumentException(
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES
					+ " invalid"); 

		// Get optional exported configs
		String[] exportedConfigs = PropertiesUtil
				.getStringArrayFromPropertyValue(overridingProperties
						.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS));
		if (exportedConfigs == null) {
			exportedConfigs = PropertiesUtil
					.getStringArrayFromPropertyValue(serviceReference
							.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS));
		}
		
		boolean configSupported = false;
		if(exportedConfigs !=null){
			for(String config : exportedConfigs){
				if(config.equals("r-osgi")){
					configSupported = true;
				}
			}
		} else {
			configSupported = true;
		}
		if(!configSupported){
			throw new IllegalArgumentException("Configurations not supported!");
		}
		
		// Get all intents (service.intents, service.exported.intents,
		// service.exported.intents.extra)
		String[] serviceIntents = PropertiesUtil.getServiceIntents(serviceReference, overridingProperties);
		
		// We don't support any intents at this moment
		if(serviceIntents!=null){
			throw new IllegalArgumentException("Intent "+serviceIntents[0]+" not supported!");
		}
		
		// Keep service id and service object
		long id = (Long)serviceReference.getProperty("service.id");
		this.serviceId = ""+id;
		this.serviceObject = context.getService(serviceReference);
		this.serviceReference = serviceReference;
		
		// Create EndpointDescriptionProperties
		createExportEndpointDescriptionProperties(serviceReference, (Map<String, Object>) overridingProperties, exportedInterfaces, serviceIntents, frameworkId);
		
		// Cache list of methods in a Map, faster lookup then reflection?
		createMethodList(serviceObject, exportedInterfaces);

	}
	
	public int acquire(){
		return ++refCount;
	}
	
	public int release(){
		return --refCount;
	}
	
	public String getServiceId(){
		return serviceId;
	}
	
	public Method getMethod(String methodSignature){
		return methodList.get(methodSignature);
	}
	
	public Object getServiceObject(){
		return serviceObject;
	}

	@Override
	public ServiceReference getExportedService() {
		return serviceReference;
	}

	@Override
	public EndpointDescription getExportedEndpoint() {
		// always re-fetch the address in order to mitigate runtime ip change
		String endpointId = "r-osgi://"+factory.getAddress()+"#"+serviceId;
		
		endpointDescriptionProperties.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
				endpointId);
		
		return new EndpointDescription(endpointDescriptionProperties);
	}
	
	
	private void createExportEndpointDescriptionProperties(
			ServiceReference serviceReference,
			Map<String, Object> overridingProperties,
			String[] exportedInterfaces, String[] serviceIntents, 
			String frameworkId) {
	
		endpointDescriptionProperties = new TreeMap<String, Object>(
				String.CASE_INSENSITIVE_ORDER);

		// OSGi properties
		// OBJECTCLASS set to exportedInterfaces
		endpointDescriptionProperties.put(
				org.osgi.framework.Constants.OBJECTCLASS, exportedInterfaces);

		// ENDPOINT_ID is refreshed each time endpoint description is collected
		// this is to mitigate possible changing ip address


		// ENDPOINT_SERVICE_ID
		// This is always set to the value from serviceReference as per 122.5.1
		Long serviceId = (Long) serviceReference
				.getProperty(org.osgi.framework.Constants.SERVICE_ID);
		endpointDescriptionProperties.put(
				org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID, serviceId);

		// ENDPOINT_FRAMEWORK_ID
		endpointDescriptionProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
						frameworkId);

		// REMOTE_CONFIGS_SUPPORTED
		String[] remoteConfigsSupported = new String[]{"r-osgi"}; //TODO make this configurable?
		if (remoteConfigsSupported != null)
			endpointDescriptionProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED,
							remoteConfigsSupported);
		
		// SERVICE_IMPORTED_CONFIGS
		endpointDescriptionProperties
				.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
						remoteConfigsSupported);

		// SERVICE_INTENTS
		Object intents = PropertiesUtil
				.getPropertyValue(
						null,
						overridingProperties,
						org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS);
		if (intents == null)
			intents = serviceIntents;
		if (intents != null)
			endpointDescriptionProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS,
							intents);

		// Finally, copy all non-reserved properties
		PropertiesUtil.copyNonReservedProperties(overridingProperties,
				endpointDescriptionProperties);
	
	}

	
	private void createMethodList(Object serviceObject, String[] exportedInterfaces){
		List<String> exportedInterfaceList = Arrays.asList(exportedInterfaces);
		for(Class iface : serviceObject.getClass().getInterfaces()){
			if(exportedInterfaceList.contains(iface.getName())){
				for(Method m : iface.getMethods()){
					methodList.put(MethodSignature.getMethodSignature(m), m);
				}
			}
		}
	}

	private boolean validExportedInterfaces(ServiceReference serviceReference,
			String[] exportedInterfaces) {
		if (exportedInterfaces == null || exportedInterfaces.length == 0)
			return false;
		List<String> objectClassList = Arrays
				.asList((String[]) serviceReference
						.getProperty(org.osgi.framework.Constants.OBJECTCLASS));
		for (int i = 0; i < exportedInterfaces.length; i++)
			if (!objectClassList.contains(exportedInterfaces[i]))
				return false;
		return true;
	}
}
