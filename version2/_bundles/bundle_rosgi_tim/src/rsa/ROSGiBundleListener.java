package rsa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ImportRegistration;

import rsa.util.EndpointDescriptionParser;

/*
 * Parse XML endpoint descriptions for importing endpoints of started bundles
 */
public class ROSGiBundleListener implements BundleListener {

	private final ROSGiServiceAdmin rsa;

	private final Map<Bundle, List<ImportRegistration>> importRegistrations;

	public ROSGiBundleListener(ROSGiServiceAdmin rsa) {
		this.rsa = rsa;
		this.importRegistrations = new HashMap<Bundle, List<ImportRegistration>>();
	}
	
	@Override
	public void bundleChanged(BundleEvent event) {
		final Bundle bundle = event.getBundle();
		switch (event.getType()) {
		case BundleEvent.STARTED: {
			List<EndpointDescription> endpointDescriptions = EndpointDescriptionParser
					.parseEndpointDescriptions(bundle);
			if (endpointDescriptions.size() > 0) {
				List<ImportRegistration> importRegistrationsList = new ArrayList<ImportRegistration>();
				for (EndpointDescription endpointDescription : endpointDescriptions) {
					ImportRegistration ir = rsa.importService(endpointDescription);
					importRegistrationsList.add(ir);
				}
				importRegistrations.put(bundle, importRegistrationsList);
			}
			break;
		}
		case BundleEvent.UNINSTALLED: {
			List<ImportRegistration> importRegistrationsList = importRegistrations.get(bundle);
			if (importRegistrationsList != null) {
				for (ImportRegistration ir : importRegistrationsList) {
					ir.close();
				}
			}
			break;
		}
		}

	}

}
