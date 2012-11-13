package be.ugent.ods.osgi.felix;

public class Config {
	
public static final String EXPORTS=
	"be.ugent.ods.testapplications.service.interfaces; "+
	"udprsa.annotation; "+
	
	// also export compendium
	"org.osgi.service.component;version=\"1.1\", "+
	"org.osgi.service.event;version=\"1.2\", "+
	"org.osgi.service.log;version=\"1.3\", "+
	"org.osgi.service.metatype;version=\"1.1\", "+
	"org.osgi.service.remoteserviceadmin;version=\"1.0.1\", "+
	"org.osgi.service.http;version=\"1.2.1\", "+
	"org.osgi.service.cm;version=\"1.4\", " +
	"org.osgi.service.deploymentadmin;version=\"1.1\", "+
	"org.osgi.service.deploymentadmin.spi;version=\"1.0.1\" "
	;
	public static String HOME_DIR= "/data/data/be.ugent.ods";
}