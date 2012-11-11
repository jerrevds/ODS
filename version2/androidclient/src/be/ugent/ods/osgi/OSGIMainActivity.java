package be.ugent.ods.osgi;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import be.ugent.ods.osgi.felix.Config;
import be.ugent.ods.osgi.felix.OSGiRuntime;
import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;
import be.ugent.ods.osgi.tests.implementations.EchoTest;
import be.ugent.ods.osgi.tests.implementations.GlowFilterTest;
import be.ugent.ods.osgi.tests.implementations.VideoTest;
import be.ugent.ods.osgi.tests.interfaces.FeedbackInterface;
import be.ugent.ods.osgi.tests.interfaces.TestInterface;
import be.ugent.ods.osgi.tests.measure.MeasurementInterface;
import be.ugent.ods.osgi.tests.measure.MeasurementTool;
import be.ugent.ods.testapplications.service.list.TestApplicationProtocolList;

public class OSGIMainActivity extends Activity implements FeedbackInterface {

	// which protocol?
	private ModuleAccessor accessor;
	
	// which test?
	private TestInterface currenttest;
	
	// other:
	private Handler UIHandler;

	private ProgressDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// INIT FELIX
		Config.HOME_DIR = getFilesDir().getAbsolutePath();
		
		OSGiRuntime osgi = OSGiRuntime.getOSGiRuntime();

		if(!osgi.isStarted()){
			osgi.start();
			
			startBundle("rosgitim", R.raw.rosgitim);
			startBundle("rosgiudp", R.raw.rosgiudp);
			startBundle("rsaother", R.raw.rsaother);
			//startBundle("greeter_interface");
			//startBundle("greeter_service");
			startBundle("serversideimplementations", R.raw.serversideimplementations);
			
		}
		
		
		// CREATE RSA MANAGER
		accessor = new ModuleAccessor(osgi);
		
		
		// CREATE THE GUI
		UIHandler = new Handler();
		
		
		//DEFAULTS
		currenttest = new EchoTest();
		accessor.setUseRSAModule(TestApplicationProtocolList.PROTOCOL_LOCAL);
		
		
		// MAKE THE BUTTONS WORK
		int[] testButtons = new int[] {R.id.toggleButton_echo, R.id.toggleButton_image, R.id.toggleButton_video};
		initButtonForTest(R.id.toggleButton_echo, new EchoTest(), testButtons); //echo test button
		initButtonForTest(R.id.toggleButton_image, new GlowFilterTest()/*TODO*/, testButtons); //image test button
		initButtonForTest(R.id.toggleButton_video, new VideoTest()/*TODO*/, testButtons); //video test button
		
		int[] rsaButtons = new int[]{R.id.toggleButton_local, R.id.toggleButton_rosgi, R.id.toggleButton_udp, R.id.toggleButton_other};
		initButtonForRSA(R.id.toggleButton_local, TestApplicationProtocolList.PROTOCOL_LOCAL, rsaButtons);
		initButtonForRSA(R.id.toggleButton_rosgi, TestApplicationProtocolList.PROTOCOL_ROSGI_TIM, rsaButtons);
		initButtonForRSA(R.id.toggleButton_udp, TestApplicationProtocolList.PROTOCOL_ROSGI_UDP, rsaButtons);
		initButtonForRSA(R.id.toggleButton_other, TestApplicationProtocolList.PROTOCOL_OTHER, rsaButtons);
		
		
		initButtonForRun(R.id.button_runtest_1, 1);
		initButtonForRun(R.id.button_runtest_5, 5);
		initButtonForRun(R.id.button_runtest_20, 20);
		
		
	
		
		
		
	}
	
	/**
	 * start a bundle
	 */
    private void startBundle(String name, int id) {
		System.out.println("Starting bundle " + name);
		InputStream is = getResources().openRawResource(id);
		try {
			org.osgi.framework.Bundle b = OSGiRuntime.getOSGiRuntime()
					.getBundleContext().installBundle(name + ".jar", is);
			b.start();
			System.out.println("Bundle " + name + " with id "+b.getBundleId()+" starting");
		} catch (Exception e) {
			System.out.println("Error starting bundle " + name);
			e.printStackTrace(System.out);
		}
	} 
	
	/**
	 * run a test
	 */
	public void runTest(final TestInterface test,final int count) {
		TextView runningview = new TextView(this);
		runningview.setText("Started running test "+test.getClass().getName()+" "+count+" times...\nPlease wait...");
		pushTestView(runningview);
		
		
		final FeedbackInterface feedback = this;
		final MeasurementInterface measurement = new MeasurementTool();
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.show();
		Thread thread = new Thread(new Runnable() {
			public void run() {
				test.runTest(accessor, feedback, measurement,count);
			}
		});

		thread.setDaemon(true);
		thread.start();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		currenttest.runActivityForResult(requestCode, resultCode, data);
	}
	
	//
	// ======= Tool methods for action listeners =======
	//
	
	/**
	 * togglegroup
	 */
	private void togglegroup(int[] allbuttons, int active) {
		for(int i=0;i<allbuttons.length;i++) {
			ToggleButton button = (ToggleButton) findViewById(allbuttons[i]);
			button.setChecked(allbuttons[i]==active);
		}
		
	}

	/**
	 * init test button
	 */
	private void initButtonForTest(final int buttonId, final TestInterface test, final int[] allButtons){
		ToggleButton button = (ToggleButton) findViewById(buttonId);
		if(allButtons[0]==buttonId){
			button.setChecked(true);
		}
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				togglegroup(allButtons, buttonId);
				currenttest = test;
			}
		});
	}
	
	/**
	 * init remotemethod button
	 */
	private void initButtonForRSA(final int buttonId, final int rsaIndex, final int[] allButtons){
		ToggleButton button = (ToggleButton) findViewById(buttonId);
		if(allButtons[0]==buttonId){
			button.setChecked(true);
		}
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				togglegroup(allButtons, buttonId);
				accessor.setUseRSAModule(rsaIndex);
			}
		});
	}
	
	/**
	 * init run button
	 */
	private void initButtonForRun(final int buttonId, final int count){
		Button run = (Button) findViewById(buttonId);
		run.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
					runTest(currenttest, count);
			}
		});
	}
	
	
	
	
	//
	// ======== GUI =========
	//

	/**
	 * standard android menu (nothing special here)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);

		return true;
	}
	
	
	/*
	 *  ======= Implementation of FeedbackInterface ==========
	 */
	
	/**
	 * methode used for tests to give a visual feedback (eg intermediar)
	 * Mark the use of a handler scince android only allows the main thread to do changes at the GUI (the handler will bring it back tot the gui thread)
	 * @param view a view to show 
	 */
	public void pushTestView(final View view){
		UIHandler.post(new Runnable() {
			
			public void run() {
				LinearLayout layout = (LinearLayout)findViewById(R.id.linearlayout_pushtestview);
				layout.removeAllViews();
				layout.removeView(view);
				layout.addView(view);
				mProgressDialog.dismiss();
			}
		});
	}
	
	public Activity getActivity() {
		return this;
	}
	
	/**
	 * method used to stop the progressdialog when a test is ready
	 *  Mark the use of a handler scince android only allows the main thread to do changes at the GUI (the handler will bring it back tot the gui thread)
	 */
	public void testReady(){
			UIHandler.post(new Runnable() {
			
			public void run() {
			//	mProgressDialog.dismiss();
			}
		});
	}
}
