package be.ugent.ods.osgi;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import org.osgi.framework.BundleException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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
	private ArrayList<org.osgi.framework.Bundle> bundles = new ArrayList<org.osgi.framework.Bundle>();

	private WakeLock wl;

	private ArrayList<TestInterface> tests;
	
	private SoundPool pool;
	private int sound_ok;
	private int sound_err;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//wakelock to keep device awake while testing
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		 wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "OSGi");
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
			startAll();
			
		}
		
		// SOUND
		pool = new SoundPool(10, AudioManager.STREAM_ALARM, 0);
		
		sound_ok = pool.load(getApplicationContext(), R.raw.soundok, 1);
		sound_err = pool.load(getApplicationContext(), R.raw.sounderr, 1);
		
		
		
		// CREATE RSA MANAGER
		accessor = new ModuleAccessor(osgi);
		
		
		// CREATE THE GUI
		UIHandler = new Handler();
		
		
		//DEFAULTS
		tests = new ArrayList<TestInterface>();
		tests.add(new EchoTest());
		tests.add(new GlowFilterTest());
		tests.add(new VideoTest());
		currenttest = tests.get(0);
		accessor.setUseRSAModule(TestApplicationProtocolList.PROTOCOL_LOCAL);
		
		
		// MAKE THE BUTTONS WORK
		int[] testButtons = new int[] {R.id.toggleButton_echo, R.id.toggleButton_image, R.id.toggleButton_video};

		initButtonForTest(R.id.toggleButton_echo, tests.get(0), testButtons); //echo test button
		initButtonForTest(R.id.toggleButton_image, tests.get(1), testButtons); //image test button
		initButtonForTest(R.id.toggleButton_video, tests.get(2), testButtons); //video test button
		
		int[] rsaButtons = new int[]{R.id.toggleButton_local, R.id.toggleButton_rosgi, R.id.toggleButton_udp, R.id.toggleButton_other};
		initButtonForRSA(R.id.toggleButton_local, TestApplicationProtocolList.PROTOCOL_LOCAL, rsaButtons);
		initButtonForRSA(R.id.toggleButton_rosgi, TestApplicationProtocolList.PROTOCOL_ROSGI_TIM, rsaButtons);
		initButtonForRSA(R.id.toggleButton_udp, TestApplicationProtocolList.PROTOCOL_ROSGI_UDP, rsaButtons);
		initButtonForRSA(R.id.toggleButton_other, TestApplicationProtocolList.PROTOCOL_OTHER, rsaButtons);
		
		
		initButtonForRun(R.id.button_runtest_1, 1);
		initButtonForRun(R.id.button_runtest_5, 5);
		initButtonForRun(R.id.button_runtest_20, 20);
		initSizebutton();
		
		
		
		// AUTOMATIC CONFIG
		Button button = (Button) findViewById(R.id.button_auto_run);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						autoInfo("AutoMeasure Starting...");
						try{
							onAutoMeasure();
						}catch(Exception ex){
							String toString = ex.getClass().getName()+": "+ex.getMessage();
							accessor.autoMeasureCrashedMeasure(toString);
							autoInfo(toString);
							return;
						}
						autoWait();
						autoInfo("AutoMeasure Done...");
					}
				}).start();
			}
		});
		
		button = (Button) findViewById(R.id.button_auto_config);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						autoInfo("AutoConfig in progress...");
						onAutoConfig();
						autoInfo("AutoConfig done...");
					}
				}).start();
			}
		});
		
		button = (Button) findViewById(R.id.button_auto_ping);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				autoInfo("ping to 192.168.56.30");
				String result = ping();
				autoInfo(result);
			}
		});
	}
	
	private void autoWait() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
	}
	
	private void autoInfo(final String info) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView view = (TextView)findViewById(R.id.auto_info);
				view.setText(info);
			}
		});
	}
	
	private void doButton(int id) {
		Button button = (Button) findViewById(id);
		button.performClick();
		try{
			Thread.sleep(1000);
		}catch (InterruptedException e) {}
	}
	
	public static String currentTestName = null;
	
	private void onAutoMeasure() {
		while(accessor.autoMeasureHasNext()) {
			accessor.autoMeasureStartMeasure();
			String[] settings = accessor.autoMeasureGetCurrentSettings().split(" ");
			final String test = settings[0];
			final String protocol = settings[1];
			final int size = Integer.parseInt(settings[2]);
			String name = settings[3];
			
			currentTestName = name;
			
			autoInfo("AutoMeasure: Running: "+currentTestName);
			
			final Semaphore guidone = new Semaphore(0);
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if(test.equals("echo")) {
						doButton(R.id.toggleButton_echo);
					}else if(test.equals("image")){
						doButton(R.id.toggleButton_image);
					}else if(test.equals("video")){
						doButton(R.id.toggleButton_video);
					}
					
					if(protocol.equals("r-osgi")) {
						doButton(R.id.toggleButton_rosgi);
					}else if(protocol.equals("udp")){
						doButton(R.id.toggleButton_udp);
					}else if(protocol.equals("restlet")){
						doButton(R.id.toggleButton_other);
					}
			
					Button sizeButton = (Button)findViewById(R.id.button_size);
					for(int i=0;i<tests.size();i++){
						tests.get(i).changeSize(size);
					}
					sizeButton.setText("s="+size);
		
					guidone.release();
				}
			});
			guidone.acquireUninterruptibly();
			
			// run the test
			runTest(currenttest, 1, true);
			
			autoInfo("AutoMeasure: Test done: "+currentTestName);
			
			currentTestName=null;
			
			accessor.autoMeasureStopMeasure();
		}
	}
	
	private void onAutoConfig(){
		ProcessBuilder builder = new ProcessBuilder("su");
		try {
			Process p = builder.start();
			
			PrintWriter pw = new PrintWriter(p.getOutputStream());
			
			try{
				pw.write("ifconfig rndis0 192.168.56.60 netmask 255.255.255.0\n");
				pw.write("exit\n");
			}finally{
				pw.close();
			}
			
			try {
				p.waitFor();
			} catch (InterruptedException e) {}
		} catch (IOException e) {
			System.err.println("Could not run commandline");
		}
	}
	
	private String ping(){
		ProcessBuilder builder = new ProcessBuilder("ping", "-c", "5", "192.168.56.30");
		try {
			Process p = builder.start();
			try {
				p.waitFor();
				BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = r.readLine();
				while(line != null) {
					if(line.contains("packet loss")){
						return "ping: "+line;
					}
					line = r.readLine();
				}
			} catch (InterruptedException e) {}
			
		} catch (IOException e) {
			System.err.println("Could not run commandline");
		}
		return "ping: no results...";
	}
	
	
	private void initSizebutton() {
		final Button sizeButton = (Button)findViewById(R.id.button_size);
		sizeButton.setOnClickListener(new OnClickListener() {
			private int size = 0;
			@Override
			public void onClick(View v) {
				size++;
				size=size%3;
				for(int i=0;i<tests.size();i++){
					tests.get(i).changeSize(size);
				}
				sizeButton.setText("s="+size);
				
			}
		});
		
	}



	@Override
	protected void onResume() {
		super.onResume();
		wl.acquire();
	}
	
	@Override
	protected void onPause() {
		
		super.onPause();
		wl.release();
	}
	
	
	private void startAll() {
		for(org.osgi.framework.Bundle bundle : bundles){
			try {
				bundle.start();
			} catch (BundleException e) {
				e.printStackTrace();
			}
		}
		
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
			bundles.add(b);
			System.out.println("Bundle " + name + " with id "+b.getBundleId()+" starting");
		} catch (Exception e) {
			System.out.println("Error starting bundle " + name);
			e.printStackTrace(System.out);
		}
	} 
    
    public void runTest(TestInterface test, int count) {
    	runTest(test, count, false);
    }
	
	/**
	 * run a test
	 */
	public void runTest(final TestInterface test,final int count, boolean wait) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView runningview = new TextView(OSGIMainActivity.this);
				runningview.setText("Started running test "+test.getClass().getName()+" "+count+" times...\nPlease wait...");
				pushTestView(runningview);
				
				mProgressDialog = new ProgressDialog(OSGIMainActivity.this);
				mProgressDialog.show();
			}
		});
		
		
		final FeedbackInterface feedback = this;
		final MeasurementInterface measurement = new MeasurementTool();
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try{
					test.runTest(accessor, feedback, measurement,count);
					playSoundFinished();
				} catch (Exception e) {
					playSoundError();
					accessor.autoMeasureCrashedMeasure(e.getClass().getName()+": "+e.getMessage());
					showErrorView(e.getMessage());
					e.printStackTrace();
				}
			}
		});

		thread.setDaemon(true);
		thread.start();
		
		if(wait) {
			try {
				thread.join();
			} catch (InterruptedException e) {}
		}
	}
	
	private void playSoundFinished(){
		//pool.play(sound_ok, 1, 1, 1, 0, 1);
	}
	
	private void playSoundError(){
		pool.play(sound_err, 1, 1, 1, 0, 1);
	}
	
	private void showErrorView(String message){
		TextView runningview = new TextView(this);
		runningview.setText("An error occured while executing the test: "+message);
		pushTestView(runningview);
		
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
