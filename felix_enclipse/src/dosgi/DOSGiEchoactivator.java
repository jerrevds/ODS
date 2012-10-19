package dosgi;

import java.util.Dictionary;
import java.util.Hashtable;

import odscommon.service.impl.EchoServiceImpl;
import odscommon.service.interfaces.EchoService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class DOSGiEchoactivator  implements BundleActivator {
	  private ServiceRegistration sr;

	  public void start(BundleContext context) throws Exception { 
	    Dictionary props = new Hashtable();
	  
	    props.put("osgi.remote.interfaces", "*");
	//    props.put("osgi.remote.configuration.type", "org.apache.cxf.ws");
	   // props.put("osgi.remote.configuration.pojo.address", "http://localhost:8888/dosgi");

	//    props.put("osgi.remote.configuration.pojo.address", "<a class="linkclass" href="http://localhost:9090/oracle">http://localhost:9090/oracle</a>");
	    sr = context.registerService(EchoService.class.getName(), 
	        new EchoServiceImpl(), props);
	  }

	  public void stop(BundleContext context) throws Exception {
	    sr.unregister();
	  }
	}