package be.ugent.ods.osgi.felix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import odscommon.service.impl.EchoServiceImpl;
import odscommon.service.interfaces.GlowFilterService;
import odscommon.service.interfaces.EchoService;

import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.ethz.iks.r_osgi.RemoteOSGiException;
import ch.ethz.iks.r_osgi.RemoteOSGiService;
import ch.ethz.iks.r_osgi.RemoteServiceReference;
import ch.ethz.iks.r_osgi.URI;

import be.ugent.ods.osgi.R;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

public class FelixManager {
	private String rootPath;

	private Felix felix;

	private Properties felixProperties;
	private ArrayList<Bundle> bundles = new ArrayList<Bundle>();
	private File bundlesDir;
	private File cacheDir;



	public FelixManager(String rootPath, Context context) {
		this.rootPath = rootPath;

		felixProperties = new FelixProperties(this.rootPath);

		bundlesDir = new File(rootPath + "/felix/bundle");
		if (!bundlesDir.exists()) {
			if (!bundlesDir.mkdirs()) {
				throw new IllegalStateException("Unable to create bundles dir");
			}
		}

		cacheDir = new File(rootPath + "/felix/cache");
		if (!cacheDir.exists()) {
			if (!cacheDir.mkdirs()) {
				throw new IllegalStateException(
						"Unable to create felixcache dir");
			}
		}

		/**
		 * activate local
		 */
		try {
			felix = new Felix(felixProperties);
			felix.start();

			// Create a property lookup service implementation.
			EchoService test = new EchoServiceImpl("local");
			// Register the property lookup service and save
			// the service registration.
			Hashtable<String, String> props = new Hashtable<String, String>();
			props.put("type", "testlocal");
			felix.getBundleContext().registerService(
					EchoService.class.getName(), test, props);
		} catch (Exception ex) {
			Log.e("OSGi", ex.toString());
		}
		/**
		 * DOSGI
		 */

		// instal dosgi bundle (single bundle crasht android)
		// don't dare to mess with the order!!!
	//	installBundle(R.raw.gogoruntime, context, 97);
		// installBundle(R.raw.gogoshell, context,96);
	//	installBundle(R.raw.orggogocommand, context, 98);
	//	installBundle(R.raw.bundlerepo, context, 1);
		/*
		 * installBundle(R.raw.compendium, context, 56); startBundles(); bundles
		 * = new ArrayList<Bundle>(); installBundle(R.raw.jaxpapi, context,
		 * 151); installBundle(R.raw.jaxpri, context, 152);
		 * installBundle(R.raw.servicemixjaxp, context, 153); startBundles();
		 * bundles = new ArrayList<Bundle>(); installBundle(R.raw.compendium,
		 * context, 56); // installBundle(R.raw.dosgi, context,99);
		 * installBundle(R.raw.dosgi13, context, 14);
		 * installBundle(R.raw.dosgi12, context, 13);
		 * installBundle(R.raw.dosgi36, context, 38);
		 * installBundle(R.raw.dosgi11, context, 12);
		 * installBundle(R.raw.dosgi7, context, 8); installBundle(R.raw.dosgi27,
		 * context, 27); installBundle(R.raw.dosgi26, context, 26);
		 * installBundle(R.raw.dosgi33, context, 34);
		 * installBundle(R.raw.dosgi32, context, 33);
		 * installBundle(R.raw.dosgi31, context, 32);
		 * installBundle(R.raw.dosgi35, context, 35);
		 * installBundle(R.raw.dosgi34, context, 36);
		 * 
		 * installBundle(R.raw.dosgi24, context, 24);
		 * installBundle(R.raw.dosgi50, context, 53);
		 * installBundle(R.raw.dosgi49, context, 52);
		 * installBundle(R.raw.dosgi20, context, 21);
		 * installBundle(R.raw.dosgi29, context, 29);
		 * installBundle(R.raw.dosgi23, context, 23);
		 * installBundle(R.raw.dosgi51, context, 54);
		 * installBundle(R.raw.dosgi30, context, 31);
		 * installBundle(R.raw.dosgi28, context, 28);
		 * installBundle(R.raw.dosgi25, context, 25);
		 * installBundle(R.raw.dosgi37, context, 38);
		 * installBundle(R.raw.dosgi38, context, 41);
		 * installBundle(R.raw.dosgi39, context, 42);
		 * installBundle(R.raw.dosgi19, context, 20);
		 * installBundle(R.raw.dosgi47, context, 50);
		 * installBundle(R.raw.dosgi46, context, 49);
		 * installBundle(R.raw.dosgi48, context, 51);
		 * installBundle(R.raw.dosgi45, context, 48);
		 * installBundle(R.raw.dosgi41, context, 44);
		 * installBundle(R.raw.dosgi40, context, 43);
		 * installBundle(R.raw.dosgi6, context, 7); installBundle(R.raw.dosgi5,
		 * context, 6); installBundle(R.raw.dosgi1, context, 2);
		 * installBundle(R.raw.dosgi43, context, 46);
		 * installBundle(R.raw.dosgi42, context, 45);
		 * installBundle(R.raw.dosgi44, context, 47);
		 * installBundle(R.raw.dosgi4, context, 5); installBundle(R.raw.dosgi2,
		 * context, 3); installBundle(R.raw.dosgi18, context, 19);
		 * installBundle(R.raw.dosgi17, context, 18);
		 * installBundle(R.raw.dosgi16, context, 17);
		 * installBundle(R.raw.dosgi14, context, 16);
		 * installBundle(R.raw.dosgi15, context, 15);
		 */

		// installBundle(R.raw.jaxb1, context, 101);
		// installBundle(R.raw.jaxb2, context, 12);
		// installBundle(R.raw.jaxb3, context, 103);
		// discovery bundle (not yet needed)
		// installBundle(R.raw.dosgi3, context,4);
		// installBundle(R.raw.dosgi8, context,9);
		// installBundle(R.raw.dosgi9, context,10);
		// installBundle(R.raw.dosgi10, context,11);
		// installBundle(R.raw.dosgi22, context,22);
		// installBundle(R.raw.dosgi35, context,37);
		// installBundle(R.raw.dosgi52, context,55);

		/*
		 * installBundle(R.raw.odscommon, context, 58);
		 * installBundle(R.raw.dosgiclient, context, 57); startBundles();
		 * ServiceReference[] refs = felix.getRegisteredServices();
		 */
		//allow internet connection on subthread
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy); 
		installBundle(R.raw.odscommon, context, 58);
		installBundle(R.raw.apachelog, context, 200);
		installBundle(R.raw.eventadmin, context, 201);
		installBundle(R.raw.androidrosgi, context, 202);
		installBundle(R.raw.rosgiclient, context, 203);
		installBundle(R.raw.asm, context, 204);
		startBundles();

		///moet naar aparte bundle om op basis van zelfde jar en classload te werken
		

	}


	
	public Felix getFelix() {
		return felix;
	}

	public Properties getFelixProperties() {
		return felixProperties;
	}

	public void stopFelix() {
		try {
			felix.stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}

	}

	public void installBundle(int id, Context context, int loadnr) {
		InputStream is = context.getResources().openRawResource(id);
		BundleContext bc = felix.getBundleContext();
		Bundle b;
		try {
			b = felix.getBundleContext().installBundle(loadnr + ".jar", is);
			Log.d("OSGi", "name:" + b.getSymbolicName());
			bundles.add(b);
			Log.d("OSGi", "bundle installed" + loadnr);
		} catch (BundleException e) {
			Log.e("OSGi",
					"Error starting bundle " + loadnr + " " + e.toString());
			e.printStackTrace();
		}

	}

	public void startBundles() {
		for (int i = 0; i < bundles.size(); i++) {
			try {
				bundles.get(i).start();
				Log.e("OSGi", "bundle started" + i);
			} catch (Exception e) {
				Log.e("OSGi", "Error starting bundle " + i + " " + e.toString());
				e.printStackTrace();

			}
		}
	}
}