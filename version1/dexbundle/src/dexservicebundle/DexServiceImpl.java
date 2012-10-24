package dexservicebundle;

import java.util.GregorianCalendar;

import org.osgi.service.log.LogService;

import com.android.dx.command.dexer.Main;

/**
 * Dex Service Implementation
 * 
 * This implementation of the DexService interface uses a modified version of 
 * dalvik exchange (dx), which is part of the Android SDK, to convert regular
 * Java bytecode to Android bytecode. It can be used inside an OSGi framework
 * on Android itself.
 * 
 * @author Stephan Heuser (stephan.heuser@sit.fraunhofer.de)
 *
 */

public class DexServiceImpl implements DexService {
	private LogService logService = null;

	public byte[] createDexedJarFromFile(String filename) {
		
		if (logService != null) {
			logService.log(LogService.LOG_INFO, "DexService: Converting " + filename + " start: " + GregorianCalendar.getInstance().getTimeInMillis());
		}
		
		byte[] dexed = Main.createDexJar(filename);
		
		if (logService != null) {
			logService.log(LogService.LOG_INFO, "DexService: Converting " + filename + " end: " + GregorianCalendar.getInstance().getTimeInMillis());
		}
		
		return dexed;
		
	}
	
	public void bindLogService(LogService log) {
		this.logService = log;
	}
	
	public void unbindLogService(LogService log) {
		this.logService = null;
	}

}
