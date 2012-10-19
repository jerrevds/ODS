package server;

import java.util.Dictionary;
import java.util.Hashtable;

import odscommon.service.impl.EchoServiceImpl;
import odscommon.service.interfaces.EchoService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator  implements BundleActivator {
	  private ServiceRegistration sr;

	  public void start(BundleContext context) throws Exception { 
	    Dictionary props = new Hashtable();
	  
	    props.put("service.exported.interfaces", "*");
        props.put("service.exported.configs", "org.apache.cxf.ws");
        props.put("org.apache.cxf.ws.address", "http://localhost:9090/dosgi");
	    sr = context.registerService(EchoService.class.getName(), 
	        new EchoServiceImpl(), props);
	  }

	  public void stop(BundleContext context) throws Exception {
	    sr.unregister();
	  }
	}