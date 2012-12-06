package be.ugent.ods.osgi.tests.interfaces;

import android.content.Intent;
import android.util.Log;
import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;
import be.ugent.ods.osgi.tests.measure.MeasurementInterface;

public abstract class AbstractTest implements TestInterface {
	
	
	
	protected FeedbackInterface feedback;
	public void runTest(ModuleAccessor accessor, FeedbackInterface feedback, MeasurementInterface measurements, int iterations){
		this.feedback = feedback;
		preRun(accessor);
		measurements.startMeasuring(iterations, feedback.getActivity(), getName()+accessor.getCurrentRsaIndex());
		for(int i = 0;i<iterations;i++){
			test();
			Log.d("ODS", "one test iter finish");
		}
		measurements.stopMeasuring();
		postRun();
		
	}
	
	public void runActivityForResult(int requestCode, int resultCode, Intent data){}
	
	public abstract void test();
	/**
	 * called befor running a test
	 * not in measruements
	 */
	public void preRun(ModuleAccessor accessor){}
	/**
	 * called after running a test, not in measruements
	 */
	public void postRun(){}
	public abstract String getName();
	
}
