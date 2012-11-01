package be.ugent.ods.osgi.tests.interfaces;

import android.content.Intent;
import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;
import be.ugent.ods.osgi.tests.measure.MeasurementInterface;

public interface TestInterface {
	
	void runTest(ModuleAccessor accessor, FeedbackInterface feedback, MeasurementInterface measurements, int iterations);
	
	void runActivityForResult(int requestCode, int resultCode, Intent data);
	
}
