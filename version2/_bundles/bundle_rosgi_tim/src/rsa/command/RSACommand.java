package rsa.command;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.felix.shell.Command;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ExportReference;
import org.osgi.service.remoteserviceadmin.ExportRegistration;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;

/*
 * 	Command for the Felix Shell to import/export services from CLI
 *  and list all exported endpoints for debugging
 */
public class RSACommand implements Command {
	
	private final BundleContext context;
	
	public RSACommand(BundleContext context){
		this.context = context;
	}
	
	@Override
	public void execute(String s, PrintStream out, PrintStream err) {

		StringTokenizer st = new StringTokenizer(s, " ");
		// ignore "rsa"
		st.nextToken();

		if(!st.hasMoreElements()){
			err.println("Incorrect command");
			err.println(getUsage());
			return;
		}
		
		ServiceReference r = context.getServiceReference(RemoteServiceAdmin.class.getName());
		if(r==null){
			err.println("No RemoteServiceAdmin present");
			return;
		}
		RemoteServiceAdmin rsa = (RemoteServiceAdmin)context.getService(r);
		

		String command = st.nextToken();
		if(command.equals("endpoints")){
			for(ExportReference export : rsa.getExportedServices()){
				out.println("ID: "+export.getExportedEndpoint().getId());
				out.print("Interfaces: ");
				for(String iface : export.getExportedEndpoint().getInterfaces()){
					out.print(iface+" ");
				}
				out.println("");
				out.println("Service ID: "+export.getExportedEndpoint().getServiceId());
				out.println("-------------");
			}
		} else if(command.equals("import")){
			// Two arguments expected
			if (st.countTokens() >= 2) {
				String uri = st.nextToken().trim();
				String clazz = st.nextToken().trim();
				
				try {
					Map<String, Object> properties = new HashMap<String, Object>();
					properties.put("endpoint.id", uri);
					properties.put("service.imported.configs", "r-osgi");
					properties.put("objectClass", new String[]{clazz});
					EndpointDescription endpoint = new EndpointDescription(properties);
					ImportRegistration ir = rsa.importService(endpoint);
					if(ir.getException()!=null){
						ir.getException().printStackTrace();
					} else {
						out.println("imported service at id "+ir.getImportReference().getImportedService().getProperty("service.id"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} else {
				err.println("Incorrect number of arguments");
				err.println("rsa import <uri> <clazz>");
			}
		} else if(command.equals("export")){
			if (st.countTokens() >= 1) {
				String service = st.nextToken().trim();
				try {
					ServiceReference toExport = context.getServiceReference(service);

					Map<String, Object> properties = new HashMap<String, Object>();
					properties.put("service.exported.interfaces", new String[]{service});
					Collection<ExportRegistration> exports = rsa.exportService(toExport, properties);
					for(ExportRegistration export : exports){
						if(export.getException()!=null){
							err.println("Error exporting service ");
							export.getException().printStackTrace(err);
						} else {
						    out.println("Exported service "+export.getExportReference().getExportedService().getProperty("service.id"));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				err.println("Incorrect number of arguments");
				err.println("rsa export <clazz>");
			}
		} else {
			err.println("Unkown command ");
			err.println(getUsage());
		}

	}

	@Override
	public String getName() {
		return "rsa";
	}

	@Override
	public String getShortDescription() {
		return "Export and import services with RemoteServiceAdmin.";
	}

	@Override
	public String getUsage() {
		return "rsa <command> [<argument> ...] \n" +
				" rsa import <uri> <clazz> \n" +
				" rsa export <clazz> \n" +
				" rsa endpoints \n";
	}
}
