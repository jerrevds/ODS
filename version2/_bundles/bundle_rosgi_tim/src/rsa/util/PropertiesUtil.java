package rsa.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/*
 * Utility class for handling OSGi properties 
 */
public class PropertiesUtil {

	protected static final List osgiProperties = Arrays
			.asList(new String[] {
					// OSGi properties
					org.osgi.framework.Constants.OBJECTCLASS,
					org.osgi.framework.Constants.SERVICE_ID,
					org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID,
					org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID,
					org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID,
					org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_CONFIGS_SUPPORTED,
					org.osgi.service.remoteserviceadmin.RemoteConstants.REMOTE_INTENTS_SUPPORTED,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS_EXTRA,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,
					org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS });


	public static String[] getStringArrayFromPropertyValue(Object value) {
		if (value == null)
			return null;
		else if (value instanceof String)
			return new String[] { (String) value };
		else if (value instanceof String[])
			return (String[]) value;
		else if (value instanceof Collection)
			return (String[]) ((Collection) value).toArray(new String[] {});
		else
			return null;
	}

	public static String[] getExportedInterfaces(
			ServiceReference serviceReference,
			Map<String, ?> overridingProperties) {
		Object overridingPropValue = overridingProperties
				.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES);
		if (overridingPropValue != null)
			return getExportedInterfaces(serviceReference, overridingPropValue);
		return getExportedInterfaces(serviceReference);
	}

	public static String[] getExportedInterfaces(
			ServiceReference serviceReference, Object propValue) {
		if (propValue == null)
			return null;
		String[] objectClass = (String[]) serviceReference
				.getProperty(org.osgi.framework.Constants.OBJECTCLASS);
		boolean wildcard = propValue.equals("*"); //$NON-NLS-1$
		if (wildcard)
			return objectClass;
		else {
			final String[] stringArrayValue = getStringArrayFromPropertyValue(propValue);
			if (stringArrayValue == null)
				return null;
			else if (stringArrayValue.length == 1
					&& stringArrayValue[0].equals("*")) { //$NON-NLS-1$
				// this will support the idiom: new String[] { "*" }
				return objectClass;
			} else
				return stringArrayValue;
		}
	}

	public static String[] getExportedInterfaces(
			ServiceReference serviceReference) {
		return getExportedInterfaces(
				serviceReference,
				serviceReference
						.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES));
	}

	public static String[] getServiceIntents(ServiceReference serviceReference,
			Map overridingProperties) {
		List results = new ArrayList();

		String[] intents = getStringArrayFromPropertyValue(overridingProperties
				.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS));
		if (intents == null) {
			intents = getStringArrayFromPropertyValue(serviceReference
					.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_INTENTS));
		}
		if (intents != null)
			results.addAll(Arrays.asList(intents));

		String[] exportedIntents = getStringArrayFromPropertyValue(overridingProperties
				.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS));
		if (exportedIntents == null) {
			exportedIntents = getStringArrayFromPropertyValue(serviceReference
					.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS));
		}
		if (exportedIntents != null)
			results.addAll(Arrays.asList(exportedIntents));

		String[] extraIntents = getStringArrayFromPropertyValue(overridingProperties
				.get(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS_EXTRA));
		if (extraIntents == null) {
			extraIntents = getStringArrayFromPropertyValue(serviceReference
					.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTENTS_EXTRA));
		}
		if (extraIntents != null)
			results.addAll(Arrays.asList(extraIntents));

		if (results.size() == 0)
			return null;
		return (String[]) results.toArray(new String[results.size()]);
	}

	public static Object getPropertyValue(ServiceReference serviceReference,
			String key) {
		return (serviceReference == null) ? null : serviceReference
				.getProperty(key);
	}

	public static Object getPropertyValue(ServiceReference serviceReference,
			Map<String, ?> overridingProperties, String key) {
		Object result = null;
		if (overridingProperties != null)
			result = overridingProperties.get(key);
		return (result != null) ? result : getPropertyValue(serviceReference,
				key);
	}

	public static boolean isOSGiProperty(String key) {
		return osgiProperties.contains(key)
				|| key.startsWith(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_PACKAGE_VERSION_);
	}

	// skip dotted (private) properties (R4.2 enterprise spec. table 122.1)
	public static boolean isPrivateProperty(String key) {
		return (key.startsWith(".")); //$NON-NLS-1$
	}

	public static boolean isReservedProperty(String key) {
		return isOSGiProperty(key) || isPrivateProperty(key);
	}
	
	public static Map createMapFromDictionary(Dictionary input) {
		if (input == null)
			return null;
		Map result = new HashMap();
		for (Enumeration e = input.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Object val = input.get(key);
			result.put(key, val);
		}
		return result;
	}

	public static Dictionary createDictionaryFromMap(Map propMap) {
		if (propMap == null)
			return null;
		Dictionary result = new Properties();
		for (Iterator i = propMap.keySet().iterator(); i.hasNext();) {
			Object key = i.next();
			Object val = propMap.get(key);
			result.put(key, val);
		}
		return result;
	}

	public static Map<String, Object> copyProperties(
			Map<String, Object> source, Map<String, Object> target) {
		for (String key : source.keySet())
			target.put(key, source.get(key));
		return target;
	}

	public static Map<String, Object> copyProperties(
			final ServiceReference serviceReference,
			final Map<String, Object> target) {
		final String[] keys = serviceReference.getPropertyKeys();
		for (int i = 0; i < keys.length; i++) {
			target.put(keys[i], serviceReference.getProperty(keys[i]));
		}
		return target;
	}

	public static Map<String, Object> copyNonReservedProperties(
			Map<String, Object> source, Map<String, Object> target) {
		for (String key : source.keySet())
			if (!isReservedProperty(key))
				target.put(key, source.get(key));
		return target;
	}
	
	public static Map<String, Object> copyNonReservedProperties(
			ServiceReference serviceReference, Map<String, Object> target) {
		String[] keys = serviceReference.getPropertyKeys();
		for (int i = 0; i < keys.length; i++)
			if (!isReservedProperty(keys[i]))
				target.put(keys[i], serviceReference.getProperty(keys[i]));
		return target;
	}

	public static Map mergeProperties(final ServiceReference serviceReference,
			final Map<String, Object> overrides) {
		return mergeProperties(copyProperties(serviceReference, new HashMap()),
				overrides);
	}

	private static Map mergeProperties(final Map<String, Object> source,
			final Map<String, Object> overrides) {

		// copy to target from service reference
		final Map target = copyProperties(source, new TreeMap<String, Object>(
				String.CASE_INSENSITIVE_ORDER));

		// now do actual merge
		final Set<String> keySet = overrides.keySet();
		for (final String key : keySet) {
			// skip keys not allowed
			if (Constants.SERVICE_ID.equals(key)
					|| Constants.OBJECTCLASS.equals(key)) {
				continue;
			}
			target.remove(key.toLowerCase());
			target.put(key.toLowerCase(), overrides.get(key));
		}

		return target;
	}
}
