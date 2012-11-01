package be.ugent.ods.osgi.tests.measure;

import android.content.Context;

public interface MeasurementInterface {
	public boolean startMeasuring(int iterations, Context context, String type);
	public void stopMeasuring();
}
