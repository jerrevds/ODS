package udprsa;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ImportReference;

import udprsa.exception.ROSGiException;
import udprsa.network.api.MessageSender;
import udprsa.network.api.NetworkChannel;
import udprsa.network.api.NetworkChannelFactory;
import udprsa.network.message.RemoteCallMessage;
import udprsa.network.message.RemoteCallResultMessage;
import udprsa.util.MethodSignature;
import udprsa.util.URI;

/*
 * Proxy object at the client side that calls the remote service.
 * 
 * A dynamic proxy object is generated that dispatches the calls over the network.
 */
public class ROSGiProxy implements InvocationHandler, ImportReference{

	private ServiceRegistration registration;
	private EndpointDescription endpointDescription;

	private String serviceId;
	private NetworkChannel channel;
	private MessageSender sender;
	
	private int refCount = 0;
	
	private ROSGiProxy(EndpointDescription endpointDescription, NetworkChannel channel, String serviceId, MessageSender sender){
		this.endpointDescription = endpointDescription;
		this.serviceId = serviceId;
		this.channel = channel;
		this.sender = sender;
	}
	
	public static ROSGiProxy createServiceProxy(BundleContext context, ClassLoader loader, EndpointDescription endpointDescription, NetworkChannelFactory channelFactory, MessageSender sender) throws ROSGiException{
		String endpointId = endpointDescription.getId();
		List<String> interfaces = endpointDescription.getInterfaces();
		
		URI uri = new URI(endpointId);
		NetworkChannel channel = channelFactory.getChannel(uri);
		String serviceId = uri.getServiceId();
		
		if(channel==null)
			throw new ROSGiException("Error creating service proxy with null channel");
		
		ROSGiProxy p = new ROSGiProxy(endpointDescription, channel, serviceId, sender);
		try {
			Class[] clazzes = new Class[interfaces.size()];
			String[] clazzNames = new String[interfaces.size()];
			for(int i=0;i<interfaces.size();i++){
				clazzNames[i] = interfaces.get(i);
				clazzes[i] = loader.loadClass(interfaces.get(i));
			}
			Object proxy = Proxy.newProxyInstance(loader, clazzes, p);
			Hashtable<String, Object> properties = new Hashtable();
			properties.put("service.imported", "true");
			// TODO filter endpointdescription properties?
			for(String key : endpointDescription.getProperties().keySet()){
				if(key!=null && endpointDescription.getProperties().get(key)!=null){
					properties.put(key, endpointDescription.getProperties().get(key));
				}
			}
			p.registration = context.registerService(clazzNames, proxy, properties);
		} catch (ClassNotFoundException e) {
			throw new ROSGiException("Error loading class of service proxy", e);
		}
		return p;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		RemoteCallMessage invokeMsg = new RemoteCallMessage(serviceId, method, args);
	
		// equals and hashcode should be invoked on proxy object
		// this enables to keep proxies in a list/map
		if(method.getName().equals("equals")){
			return this.equals(args[0]);
		} else if(method.getName().equals("hashCode")){
			return this.hashCode();
		}
		
		try {
			// send the message and get a RemoteCallResultMessage in return
			RemoteCallResultMessage resultMsg = (RemoteCallResultMessage) sender.sendAndWaitMessage(invokeMsg, channel);
			if (resultMsg.causedException()) {
				//udp could mess things up
				return null;
			}
			Object result = resultMsg.getResult();
			return result;
			
		} catch (ROSGiException e) {
			// For now just leave registration open and throw exception
			// The one capturing the exception should is responsible for unregistering?
			// unregister();
			//udp could mess things up
			return null;
		}
	}

	
	public int acquire(){
		return ++refCount;
	}
	
	public int release(){
		return --refCount;
	}
	
	public void unregister(){
		if(registration!=null){
			synchronized(registration){
				if(registration!=null){
					try {
						registration.unregister();
					}catch(IllegalStateException e){
						// was already unregistred (e.g. by stopping framework)
					}
					registration = null;
				}
			}
	
		}
	}

	public NetworkChannel getNetworkChannel(){
		return channel;
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
