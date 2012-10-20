package be.ugent.ods.osgi;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import be.ugent.ods.osgi.felix.FelixManager;
import be.ugent.ods.osgi.tests.EchoTest;
import be.ugent.ods.osgi.tests.TestInterface;

public class OSGIMainActivity extends Activity {
	private String source = "local";
	private Button testTestButton;
	private FelixManager felixManager;
//	private ProgressDialog mProgressDialog;
	private Handler UIHandler;

	private TestInterface test;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String packageRootPath = getFilesDir().getAbsolutePath();
		 felixManager = new FelixManager(packageRootPath, this);
		setButtons();
		setTestbuttons();
		UIHandler = new Handler();
		test = new EchoTest(felixManager);
	}

	/**
	 * sets the button so that onlcik the initialised test will run
	 */
	private void setTestbuttons() {
		testTestButton = (Button) findViewById(R.id.button_runtest);
		testTestButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
			//	mProgressDialog = new ProgressDialog(OSGIMainActivity.this);
			//	mProgressDialog.setTitle("working");
			//	mProgressDialog.show();
				test.runTest(source, OSGIMainActivity.this);
				
			}
		});
		
	}
/**
 * method to set the toggle buttons ath the lower level of the screen
 * gives the effect when clicking one button the others will go out
 * sets the correct source
 * sets the correct tests
 */
	private void setButtons() {
		/**
		 * remote versions buttons
		 */
		final ToggleButton local = (ToggleButton) findViewById(R.id.toggleButton_local);
		final ToggleButton rosgi = (ToggleButton) findViewById(R.id.toggleButton_rosgi);
		final ToggleButton dosgi = (ToggleButton) findViewById(R.id.toggleButton_dosgi);
		local.setChecked(true);
		local.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				source = "local";
				rosgi.setChecked(false);
				dosgi.setChecked(false);

			}
		});
		dosgi.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				source = "dosgi";
				rosgi.setChecked(false);
				local.setChecked(false);

			}
		});
		rosgi.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				source = "rosgi";
				local.setChecked(false);
				dosgi.setChecked(false);

			}
		});

		
		/**
		 * tets versions buttons
		 */
		final ToggleButton echo = (ToggleButton) findViewById(R.id.toggleButton_echo);
		final ToggleButton image = (ToggleButton) findViewById(R.id.toggleButton_image);
		final ToggleButton video = (ToggleButton) findViewById(R.id.toggleButton_video);
		echo.setChecked(true);
		echo.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				test = new EchoTest(felixManager);
				image.setChecked(false);
				video.setChecked(false);

			}
		});
		image.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//test = new ImageTest(felixManager);
				echo.setChecked(false);
				video.setChecked(false);

			}
		});
		video.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
			//	test = new VideoTest(felixManager);
				echo.setChecked(false);
				image.setChecked(false);

			}
		});

	}

	/**
	 * standard android menu (nothing special here)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);

		return true;
	}
	
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
				layout.addView(view);
			//	mProgressDialog.dismiss();
			}
		});
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
