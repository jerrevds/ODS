package be.ugent.ods.osgi.tests.interfaces;

import android.app.Activity;
import android.content.Intent;
import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;
import be.ugent.ods.osgi.tests.measure.MeasurementInterface;

public abstract class TestInterface {
	
	
	
	protected FeedbackInterface feedback;
	protected boolean waitingForResult=false;
	public void runTest(ModuleAccessor accessor, FeedbackInterface feedback, MeasurementInterface measurements, int iterations){
		this.feedback = feedback;
		preRun(accessor);
		
		while(waitingForResult){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		measurements.startMeasuring(iterations, feedback.getActivity(), getName()+accessor.getCurrentRsaIndex());
		for(int i = 0;i<iterations;i++){
			test();
		}
		measurements.stopMeasuring();
		postRun();
		
	}
	
	public abstract void runActivityForResult(int requestCode, int resultCode, Intent data);
	
	public abstract void test();
	/**
	 * called befor running a test
	 * not in measruements
	 */
	public abstract void preRun(ModuleAccessor accessor);
	/**
	 * called after running a test, not in measruements
	 */
	public abstract void postRun();
	public abstract String getName();
	
}
