package rsa;

import java.io.IOException;
import java.io.NotSerializableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointPermission;
import org.osgi.service.remoteserviceadmin.ExportReference;
import org.osgi.service.remoteserviceadmin.ExportRegistration;
import org.osgi.service.remoteserviceadmin.ImportReference;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import rsa.exception.ROSGiException;
import rsa.network.TCPChannelFactory;
import rsa.network.api.MessageReceiver;
import rsa.network.api.MessageSender;
import rsa.network.api.NetworkChannel;
import rsa.network.api.NetworkChannelFactory;
import rsa.network.message.ROSGiMessage;
import rsa.network.message.RemoteCallMessage;
import rsa.network.message.RemoteCallResultMessage;

/*
 * Main class ... implements RemoteServiceAdmin, and implements the messaging protocol
 */
public class ROSGiServiceAdmin implements RemoteServiceAdmin, MessageReceiver, MessageSender {
	
	BundleContext context;
	
	NetworkChannelFactory channelFactory;
	
	ExecutorService messageHandler;
	
	ServiceTracker remoteServiceAdminListenerTracker;
	ServiceTracker eventAdminTracker;
	
	ServiceTracker serviceTracker;
	
	// Exported Services (mapped by serviceId)
	Map<String, ROSGiEndpoint> endpoints = Collections.synchronizedMap(new HashMap<String, ROSGiEndpoint>());
	
	// Imported Services (mapped by endpointId)
	Map<String, ROSGiProxy> proxies = Collections.synchronizedMap(new HashMap<String, ROSGiProxy>());
	
	public ROSGiServiceAdmin(BundleContext context){
		this.context = context;
		this.messageHandler = Executors.newCachedThreadPool();
	}
	
	public void activate() throws ROSGiException{
		remoteServiceAdminListenerTracker = new ServiceTracker(
				context, RemoteServiceAdminListener.class.getName(), null);
		remoteServiceAdminListenerTracker.open();
		
		eventAdminTracker = new ServiceTracker(context,
				EventAdmin.class.getName(), null);
		eventAdminTracker.open();
		
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
		
		try {
			channelFactory = new TCPChannelFactory(this, networkInterface, port);
			channelFactory.activate();
		} catch(IOException e){
			throw new ROSGiException("Failed to create Channel Factory", e);
		}
		
		/*
		 * ServiceListener that automatically exports services with exported interfaces
		 * as defined by the Remote Services specification
		 */
		try {
			serviceTracker = new ServiceTracker(context,
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
	
	public void deactivate(){
		serviceTracker.close();
		
		try {
			channelFactory.deactivate();
		} catch(IOException e){
			e.printStackTrace();
		}
		
		remoteServiceAdminListenerTracker.close();
		eventAdminTracker.close();
	}
	
	/*
	 * Methods implemented from the OSGi Remote Service Admin specification
	 */

	@Override
	public Collection<ExportRegistration> exportService(
			ServiceReference serviceReference, Map<String, ?> overridingProperties) {
		Collection<ExportRegistration> exportRegistrations = new ArrayList<ExportRegistration>();
		
		ROSGiExportRegistration registration;
		synchronized(endpoints){
			try {
				long id = (Long)serviceReference.getProperty("service.id");
				String serviceId = ""+id;
				System.out.println("Adding service :\""+serviceId+"\"");
			
				ROSGiEndpoint endpoint = endpoints.get(serviceId); 
				if(endpoint==null){
					endpoint = new ROSGiEndpoint(context, 
							serviceReference, 
							overridingProperties, 
							getFrameworkUUID(context), 
							channelFactory);
					
					checkEndpointPermission(endpoint.getExportedEndpoint(),
							EndpointPermission.EXPORT);
					
					endpoints.put(serviceId, endpoint);
				}
				
				registration = new ROSGiExportRegistration(endpoint);
			} catch(Throwable t){
				((Map<String, String>)overridingProperties).put(RemoteConstants.ENDPOINT_ID,"noendpoint"); 
				((Map<String, String>)overridingProperties).put(RemoteConstants.SERVICE_IMPORTED_CONFIGS,"noconfig");
				registration = new ROSGiExportRegistration(t, new EndpointDescription(serviceReference, overridingProperties));
				throw new RuntimeException(t);
			}
			exportRegistrations.add(registration);

			publishExportEvent(registration);
		}
		
		return exportRegistrations;
	}

	
	// ExportRegistration class : acquire and release the endpoints
	private class ROSGiExportRegistration implements ExportRegistration {

		ROSGiEndpoint endpoint;
		Throwable exception;
		private EndpointDescription errorEndpointDescription;
		
		
		public ROSGiExportRegistration(ROSGiEndpoint e){
			endpoint = e;
			endpoint.acquire();
		}
		
		public ROSGiExportRegistration(Throwable t, EndpointDescription endpointDescription){
			exception = t;
			errorEndpointDescription = endpointDescription;
		}
		
		@Override
		public ExportReference getExportReference() {
			return endpoint;
		}

		@Override
		public void close() {
			synchronized(endpoints){
				if(endpoint!=null && endpoint.release()==0){
					endpoints.remove(endpoint.getServiceId());
					
					RemoteServiceAdminEvent event = new RemoteServiceAdminEvent(RemoteServiceAdminEvent.EXPORT_UNREGISTRATION,
							context.getBundle(), endpoint, exception);
					publishEvent(event, endpoint.getExportedEndpoint());
					publishEventAsync(event, endpoint.getExportedEndpoint());
				}
			}
			endpoint = null;
			exception = null;
		}

		@Override
		public Throwable getException() {
			return exception;
		}
		
		public EndpointDescription getEndpointDescription() {
			return (endpoint == null) ? errorEndpointDescription
					: endpoint.getExportedEndpoint();
		}
	}
	
	@Override
	public ImportRegistration importService(EndpointDescription endpointDescription) {
		checkEndpointPermission(endpointDescription, EndpointPermission.IMPORT);
		
		String endpointId = endpointDescription.getId();
		
		ROSGiImportRegistration registration;
		
		synchronized(proxies){
			try {
				ROSGiProxy proxy = proxies.get(endpointId);
				if(proxy==null){
					proxy = ROSGiProxy.createServiceProxy(context, 
							this.getClass().getClassLoader(), 
							endpointDescription, 
							channelFactory, 
							this);
					proxies.put(endpointId, proxy);
				}
				registration = new ROSGiImportRegistration(proxy);
			} catch(ROSGiException roe){
				//roe.printStackTrace();
				registration = new ROSGiImportRegistration(roe, endpointDescription);
			}
		}
		publishImportEvent(registration);
		return registration;
	}
	
	// ImportRegistration class : aqcuire and release the proxies
	private class ROSGiImportRegistration implements ImportRegistration {

		ROSGiProxy proxy;
		Throwable exception;
		EndpointDescription errorEndpointDescription;
		
		public ROSGiImportRegistration(ROSGiProxy proxy){
			this.proxy = proxy;
			proxy.acquire();
		}
		
		public ROSGiImportRegistration(Throwable t, EndpointDescription endpointDescription){
			exception = t;
			errorEndpointDescription = endpointDescription;
		}
		
		@Override
		public ImportReference getImportReference() {
			return proxy;
		}

		@Override
		public void close() {
			if(proxy!=null){
				synchronized(proxies){
					if(proxy.release()==0){
						proxies.remove(proxy.getImportedEndpoint().getId());
						proxy.unregister();
						
						RemoteServiceAdminEvent event = new RemoteServiceAdminEvent(RemoteServiceAdminEvent.IMPORT_UNREGISTRATION,
								context.getBundle(), proxy, exception);
						publishEvent(event, proxy.getImportedEndpoint());
						publishEventAsync(event, proxy.getImportedEndpoint());
					}
				}
			}
			proxy = null;
			exception = null;
		}

		@Override
		public Throwable getException() {
			return exception;
		}
		
		public EndpointDescription getEndpointDescription() {
			return (proxy == null) ? errorEndpointDescription
					: proxy.getImportedEndpoint();
		}
	}

	@Override
	public Collection<ExportReference> getExportedServices() {
		Collection<ExportReference> results = new ArrayList<ExportReference>();
		synchronized (endpoints) {
			for (ROSGiEndpoint endpoint : endpoints.values()) {
				ExportReference eRef = (ExportReference) endpoint;
				
				if (eRef != null){
					try {
						checkEndpointPermission(eRef.getExportedEndpoint(), EndpointPermission.READ);
						results.add(eRef);
					}catch (SecurityException e) {
						// not allowed 
					}
				}
			}
		}
		return results;
	}

	@Override
	public Collection<ImportReference> getImportedEndpoints() {
		Collection<ImportReference> results = new ArrayList<ImportReference>();
		synchronized (proxies) {
			for (ROSGiProxy proxy : proxies.values()) {
				ImportReference iRef = (ImportReference) proxy;
				
				if (iRef != null){
					try {
						checkEndpointPermission(iRef.getImportedEndpoint(), EndpointPermission.READ);
						results.add(iRef);
					}catch (SecurityException e) {
						// not allowed
					}
				}
			}
		}
		return results;
	}

	private void checkEndpointPermission(
			EndpointDescription endpointDescription,
			String permissionType) throws SecurityException {
		SecurityManager sm = System.getSecurityManager();
		if (sm == null)
			return ;
		sm.checkPermission(new EndpointPermission(endpointDescription,
				getFrameworkUUID(context), permissionType));	
	}
	
	private String getFrameworkUUID(BundleContext context) {
		if (context == null)
			return null;
		return context.getProperty("org.osgi.framework.uuid");
	}
	
	
	
	/*
	 * Helper methods for sending notification events
	 */
	private void publishImportEvent(ROSGiImportRegistration importRegistration){
		Throwable exception = importRegistration.getException();
		ImportReference importReference = (exception == null) ? importRegistration.getImportReference() : null;
		
		RemoteServiceAdminEvent event = new RemoteServiceAdminEvent(
				(exception == null) ? RemoteServiceAdminEvent.IMPORT_REGISTRATION
						: RemoteServiceAdminEvent.IMPORT_ERROR, context.getBundle(),
				importReference, exception);
		
		publishEvent(event, importRegistration.getEndpointDescription());
		publishEventAsync(event, importRegistration.getEndpointDescription());
	}
	
	private void publishExportEvent(ROSGiExportRegistration exportRegistration){
		Throwable exception = exportRegistration.getException();
		ExportReference exportReference = (exception == null) ? exportRegistration.getExportReference() : null;
		
		RemoteServiceAdminEvent event = new RemoteServiceAdminEvent(
				(exception == null) ? RemoteServiceAdminEvent.EXPORT_REGISTRATION
						: RemoteServiceAdminEvent.EXPORT_ERROR, context.getBundle(),
				exportReference, exception);
		
		publishEvent(event, exportRegistration.getEndpointDescription());
		publishEventAsync(event, exportRegistration.getEndpointDescription());
	}
	
	private void publishEvent(RemoteServiceAdminEvent event, EndpointDescription endpointDescription){
		/*
		 * Synchronous events (RemoteServiceAdminListener)
		 */
		EndpointPermission perm = new EndpointPermission(endpointDescription,
				getFrameworkUUID(context),
				EndpointPermission.READ);
		
		ServiceReference[] unfilteredRefs = remoteServiceAdminListenerTracker.getServiceReferences();
		if (unfilteredRefs == null)
			return;
		
		// Filter by Bundle.hasPermission
		List<ServiceReference> filteredRefs = new ArrayList<ServiceReference>();
		for (ServiceReference ref : unfilteredRefs)
			if (ref.getBundle().hasPermission(perm))
				filteredRefs.add(ref);
		
		for (ServiceReference ref : filteredRefs) {
			RemoteServiceAdminListener l = (RemoteServiceAdminListener) remoteServiceAdminListenerTracker.getService(ref);
			if (l != null)
				l.remoteAdminEvent(event);
		}	
	
	}
	
	private void publishEventAsync(RemoteServiceAdminEvent event, EndpointDescription endpointDescription){
		/*
		 * Asynchronous events (EventAdmin)
		 */
		EventAdmin eventAdmin = (EventAdmin) eventAdminTracker.getService();
		if(eventAdmin == null)
			return;
		
		int eventType = event.getType();
		String eventTypeName = null;
		String registrationTypeName = null;
		switch (eventType) {
		case (RemoteServiceAdminEvent.EXPORT_REGISTRATION):
			eventTypeName = "EXPORT_REGISTRATION"; 
			registrationTypeName = "export.registration";
			break;
		case (RemoteServiceAdminEvent.EXPORT_ERROR):
			eventTypeName = "EXPORT_ERROR"; 
			registrationTypeName = "export.registration";
			break;
		case (RemoteServiceAdminEvent.EXPORT_UNREGISTRATION):
			eventTypeName = "EXPORT_UNREGISTRATION"; 
			registrationTypeName = "export.registration";
			break;
		case (RemoteServiceAdminEvent.EXPORT_WARNING):
			eventTypeName = "EXPORT_WARNING"; 
			registrationTypeName = "export.registration";
			break;
		case (RemoteServiceAdminEvent.IMPORT_REGISTRATION):
			eventTypeName = "IMPORT_REGISTRATION"; 
			registrationTypeName = "import.registration";
			break;
		case (RemoteServiceAdminEvent.IMPORT_ERROR):
			eventTypeName = "IMPORT_ERROR"; 
			registrationTypeName = "import.registration";
			break;
		case (RemoteServiceAdminEvent.IMPORT_UNREGISTRATION):
			eventTypeName = "IMPORT_UNREGISTRATION"; 
			registrationTypeName = "import.registration";
			break;
		case (RemoteServiceAdminEvent.IMPORT_WARNING):
			eventTypeName = "IMPORT_WARNING"; 
			registrationTypeName = "import.registration";
			break;
		}
		if (eventTypeName == null) {
			return;
		}
		String topic = "org/osgi/service/remoteserviceadmin/" + eventTypeName; 
		Bundle bundle = context.getBundle();
		Dictionary eventProperties = new Properties();
		// Bundle info
		eventProperties.put("bundle", bundle); 
		eventProperties.put("bundle.id", 
				new Long(bundle.getBundleId()));
		eventProperties.put("bundle.symbolicname", 
				bundle.getSymbolicName());
		eventProperties.put("bundle.version", bundle.getVersion()); 
		// Bundle signers
		List<String> signersList = new ArrayList<String>();
		Map signersMap = bundle.getSignerCertificates(Bundle.SIGNERS_ALL);
		for (Iterator i = signersMap.keySet().iterator(); i.hasNext();)
			signersList.add(i.next().toString());
		String[] signers = (String[]) signersList.toArray(new String[signersList.size()]);
		if (signers != null && signers.length > 0)
			eventProperties.put("bundle.signer", signers); 
		// Exception
		Throwable t = event.getException();
		if (t != null)
			eventProperties.put("cause", t); 
		// Endpoint info
		long serviceId = endpointDescription.getServiceId();
		if (serviceId != 0)
			eventProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID,
							new Long(serviceId));
		String frameworkUUID = endpointDescription.getFrameworkUUID();
		if (frameworkUUID != null)
			eventProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
							frameworkUUID);
		String endpointId = endpointDescription.getId();
		if (endpointId != null)
			eventProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
							endpointId);
		List<String> interfaces = endpointDescription.getInterfaces();
		if (interfaces != null && interfaces.size() > 0)
			eventProperties.put(org.osgi.framework.Constants.OBJECTCLASS,
					interfaces.toArray(new String[interfaces.size()]));
		List<String> importedConfigs = endpointDescription
				.getConfigurationTypes();
		if (importedConfigs != null && importedConfigs.size() > 0)
			eventProperties
					.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
							importedConfigs.toArray(new String[importedConfigs
									.size()]));
		
		eventProperties.put("timestamp", new Long(new Date().getTime())); //$NON-NLS-1$
		eventProperties.put("event", event); //$NON-NLS-1$
		if (registrationTypeName != null) {
			eventProperties.put(registrationTypeName, endpointDescription);
		}
		
		eventAdmin.postEvent(new Event(topic, eventProperties));
	}
	
	
	/*
	 * Methods for sending and receiving messages (MessageSender / MessageReceiver)
	 */
	static int xIdCount =0;
	static int MAX_THREADS = 2;
	static int TIMEOUT = 15000;

	protected final Map callbacks = new HashMap(0);
	
	private static int nextXid(){
		return ++xIdCount;
	}
	
	/*
	 * This method is called back by the NetworkChannel when a message is received
	 */
	public void receivedMessage(final ROSGiMessage msg, final NetworkChannel networkChannel) {
//		System.out.println("RECEIVED "+msg);
		
		if(msg==null){
			// Error ... clean up channel
			disposeChannel(networkChannel);
			return;
		}
	
		Runnable r = new Runnable() {
			public void run() {
				ROSGiMessage reply = handleMessage(msg);
				if (reply != null) {
					try {
						networkChannel.sendMessage(reply);
					} catch (NotSerializableException nse) {
						
					} catch (IOException ioe) {
						disposeChannel(networkChannel);
					}
				}
			}
		};
		messageHandler.execute(r);
	}

	/*
	 * Send the ROSGiMessage over the NetworkChannel
	 */
	public void sendMessage(final ROSGiMessage msg, NetworkChannel networkChannel) throws ROSGiException{
//		System.out.println("SEND "+msg);
		
		if (msg.getXID() == 0) {
			msg.setXID(nextXid());
		}

		try {
			networkChannel.sendMessage(msg);
			// TODO first try again in case of first IOException?
			return;
		} catch (final NotSerializableException nse) {
			nse.printStackTrace();
			throw new ROSGiException("Not Serializable", nse);
		} catch (final IOException ioe) {
			// failed to reconnect...
			disposeChannel(networkChannel);
			throw new ROSGiException("Network error", ioe);
		}
	}
	
	/*
	 * Send the ROSGiMessage over the NetworkChannel and wait (blocking) for reply
	 */
	public ROSGiMessage sendAndWaitMessage(final ROSGiMessage msg, NetworkChannel networkChannel) throws ROSGiException {
		if (msg.getXID() == 0) {
			msg.setXID(nextXid());
		}
		Integer xid = new Integer(msg.getXID());
		WaitingCallback blocking = new WaitingCallback();

		synchronized (callbacks) {
			callbacks.put(xid, blocking);
		}

		sendMessage(msg, networkChannel);

		// wait for the reply
		synchronized (blocking) {
			long timeout = System.currentTimeMillis() + TIMEOUT;
			ROSGiMessage result = blocking.getResult();
			try {
				while (result == null
						&& System.currentTimeMillis() < timeout) {
					blocking.wait(TIMEOUT);
					result = blocking.getResult();
				}
			} catch (InterruptedException ie) {
				throw new ROSGiException("Interrupted while waiting for callback", ie);
			}
			if (result != null) {
				return result;
			} else {
				throw new ROSGiException("No (valid) message returned");
			}
		}
	}
	
	
	class WaitingCallback {

		private ROSGiMessage result;

		public synchronized void result(ROSGiMessage msg) {
			result = msg;
			this.notifyAll();
		}

		synchronized ROSGiMessage getResult() {
			return result;
		}
	}
	
	
	/*
	 * Handle incoming messages
	 */
	private ROSGiMessage handleMessage(final ROSGiMessage msg) {

		switch (msg.getFuncID()) {
		
		case ROSGiMessage.REMOTE_CALL: {
			final RemoteCallMessage invMsg = (RemoteCallMessage) msg;
			try {
				String serviceId = invMsg.getServiceID();
				ROSGiEndpoint endpoint = endpoints.get(serviceId);
	
				if(endpoint == null){
					// no endpoint exists
					throw new ROSGiException("No valid endpoint for service "+serviceId);
				}

				// get the invocation arguments and the local method
				final Object[] arguments = invMsg.getArgs();

				final Method method = endpoint.getMethod(invMsg.getMethodSignature());
				if(method==null){
					throw new ROSGiException("No method found with signature "+invMsg.getMethodSignature()+" for endpoint service id "+endpoint.getServiceId());
				}
				
				// invoke method
				try {
					Object result = method.invoke(endpoint.getServiceObject(),
							arguments);
					final RemoteCallResultMessage m = new RemoteCallResultMessage(result);
					m.setXID(invMsg.getXID());
					return m;
				} catch (final InvocationTargetException t) {
					throw t.getTargetException();
				}
			} catch (final Throwable t) {
				RemoteCallResultMessage m = new RemoteCallResultMessage(t);
				m.setXID(invMsg.getXID());
				return m;
			}
		}
		case ROSGiMessage.REMOTE_CALL_RESULT:
			Integer xid = new Integer(msg.getXID());
			WaitingCallback callback;
			synchronized (callbacks) {
				callback = (WaitingCallback) callbacks.remove(xid);
			}
			if (callback != null) {
				callback.result(msg);
			}
			return null;
		default:
			//System.out.println("Unimplemented message type");
			return null;
		}
	}

	
	public void disposeChannel(NetworkChannel networkChannel){
		synchronized(proxies){
			Iterator it = proxies.keySet().iterator();
			while(it.hasNext()){
				ROSGiProxy proxy = proxies.get(it.next());
				if(proxy.getNetworkChannel() == networkChannel){
					it.remove();
					try {
						proxy.unregister();
					} catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
		channelFactory.deleteChannel(networkChannel);
	}
}
