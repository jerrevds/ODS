package be.ugent.ods.testapplications.service.interfaces;

import udprsa.annotation.UDP;

public interface EchoService {
	@UDP
	public String echoString(String name);
	
	public boolean autoMeasureHasNext();
	
	public void autoMeasureStartMeasure();
	
	public String autoMeasureGetCurrentSettings();
	
	public void autoMeasureStopMeasure();
	
	public void autoMeasureCrashedMeasure(String error);
	
	public void autoMeasureDumpData(String prefix, String suffix, String data);
}
