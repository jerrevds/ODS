package dexservicebundle;

/**
 * 
 * This Interface describes a service to convert regular Java Bytecode to
 * Android Bytecode. An Implementation of this service is used by the modified
 * R-OSGi Bundle to convert Proxy Bundles, which are created using ASM, to the
 * Android Bytecode format before loading the proxy bundle.
 * 
 * @author Stephan Heuser (stephan.heuser@sit.fraunhofer.de)
 *
 */

public interface DexService {
	
	/**
	 * Convert the bytecode of a given jar file into the dalvik DEX format.
	 * 
	 * @param filename Filename of the jar which should be converted
	 * @return A byte array representation of the jar file including the dex
	 * file.
	 */
	
	public byte[] createDexedJarFromFile(String filename);
	
}
