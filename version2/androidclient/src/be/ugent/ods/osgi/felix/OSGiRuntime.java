package be.ugent.ods.osgi.felix;

import java.io.File;
import java.util.Properties;

import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class OSGiRuntime {
  
	private static OSGiRuntime osgi = null;
	private static Properties props = new Properties();
	
	private Felix felix;
      
	private void delete(File target) {
    	if (target.isDirectory()) {
    		for (File file : target.listFiles()) {
    			delete(file);
    		}
    	}
    	target.delete();
    }
	
	private OSGiRuntime(){
		// init OSGi runtime
		System.out.println("Init OSGi Runtime");

		props.put("platform", "android");
		
		// for now put it on sd card / better option the application file dir?
		props.put("org.osgi.framework.storage", Config.HOME_DIR+"/cache");
		props.put("felix.cache.rootdir", Config.HOME_DIR);
		
		// delete cache ... only for development?
		File cache = new File(Config.HOME_DIR+"/cache");
		if (cache.exists()) 
        	delete(cache);
	
		props.put("org.osgi.framework.system.packages.extra", ANDROID_FRAMEWORK_PACKAGES_ext+Config.EXPORTS);
		props.put("felix.log.level", "1");
		props.put("felix.startlevel.bundle", "1");
		
		File log = new File(Config.HOME_DIR+"/log");
		if(!log.exists())
			log.mkdir();
		props.put("log.dir", Config.HOME_DIR+"/log");

		try{
			felix = new Felix(props);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Error starting Felix");
		}
	}
	
	public void start(){
		if(felix!=null){
			try {
				felix.start();
			} catch (BundleException e) {
				e.printStackTrace();
			}
		} 
	}
	
	public void stop(){
		System.out.println("Stopping OSGi...");
		try {
			if(felix!=null){
				felix.stop();
				felix.waitForStop(10000);
			} 
		}catch (Exception e) {
				e.printStackTrace();
		} finally {
			osgi = null;
			System.out.println("OSGi stopped!");
		}
	}
	
	public boolean isStarted(){
		if(felix!=null){
			int state = felix.getState();
			if(state == Bundle.STARTING || state== Bundle.ACTIVE){
				return true;
			}
		}
		return false;
	}
	
	public static Object getProperty(Object key){
		return props.get(key);
	}
	
	public static void setProperty(Object key, Object value){
		props.put(key, value);
	}
	
	public static void addPoperties(Properties p){
		for(Object k : p.keySet()){
			props.put(k, p.get(k));
		}
	}
	
	public static OSGiRuntime getOSGiRuntime(){
		if(osgi==null){
			synchronized(OSGiRuntime.class){
				if(osgi==null){
					osgi = new OSGiRuntime();
				}
			}
		}
		return osgi;
	}
	
	public BundleContext getBundleContext(){
		return felix.getBundleContext();
	}

	private static final String ANDROID_FRAMEWORK_PACKAGES_ext = (
			"org.osgi.framework; version=1.4.0," +
            "org.osgi.service.packageadmin; version=1.2.0," +
            "org.osgi.service.startlevel; version=1.0.0," +
            "org.osgi.service.url; version=1.0.0," +
            "org.osgi.util.tracker," +
            // ANDROID (here starts semicolon as separator -> Why?
            "android; " + 
            "android.app;" + 
            "android.content;" + 
            "android.content.pm;" + 
            "android.database;" + 
            "android.database.sqlite;" + 
            "android.graphics; " + 
            "android.graphics.drawable; " + 
            "android.graphics.drawable.shapes; " + 
            "android.graphics.glutils; " + 
            "android.hardware; " + 
            "android.location; " + 
            "android.media; " + 
            "android.net; " + 
            "android.opengl; " + 
            "android.os; " + 
            "android.provider; " + 
            "android.sax; " + 
            "android.speech.recognition; " + 
            "android.telephony; " + 
            "android.telephony.gsm; " + 
            "android.text; " + 
            "android.text.method; " + 
            "android.text.style; " + 
            "android.text.util; " + 
            "android.util; " + 
            "android.view; " + 
            "android.view.animation; " + 
            "android.webkit; " + 
            "android.widget; " + 
            //MAPS
            "com.google.android.maps; " + 
            "com.google.android.xmppService; " + 
            // JAVAx
            "javax.crypto; " + 
            "javax.crypto.interfaces; " + 
            "javax.crypto.spec; " + 
            "javax.microedition.khronos.opengles; " + 
            "javax.microedition.khronos.egl; " + 
            "javax.net; " + 
            "javax.net.ssl; " + 
            "javax.security.auth; " + 
            "javax.security.auth.callback; " + 
            "javax.security.auth.login; " + 
            "javax.security.auth.x500; " + 
            "javax.security.cert; " + 
            "javax.sound.midi; " + 
            "javax.sound.midi.spi; " + 
            "javax.sound.sampled; " + 
            "javax.sound.sampled.spi; " + 
            "javax.sql; " + 
            "javax.xml.parsers; " + 
            //JUNIT
            "junit.extensions; " + 
            "junit.framework; " + 
            //APACHE
            "org.apache.commons.codec; " + 
            "org.apache.commons.codec.binary; " + 
            "org.apache.commons.codec.language; " + 
            "org.apache.commons.codec.net; " + 
            "org.apache.commons.httpclient; " + 
            "org.apache.commons.httpclient.auth; " + 
            "org.apache.commons.httpclient.cookie; " + 
            "org.apache.commons.httpclient.methods; " + 
            "org.apache.commons.httpclient.methods.multipart; " + 
            "org.apache.commons.httpclient.params; " + 
            "org.apache.commons.httpclient.protocol; " + 
            "org.apache.commons.httpclient.util; " + 
            
            //OTHERS
            "org.bluez; " + 
            "org.json; " +
            "org.w3c.dom; " + 
            "org.xml.sax; " + 
            "org.xml.sax.ext; " + 
            "org.xml.sax.helpers; " + 
            "org.kxml2.io; "+
            "org.xmlpull.v1; " 
            
			).intern(); 
}
