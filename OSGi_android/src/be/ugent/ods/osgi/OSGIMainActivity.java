package be.ugent.ods.osgi;


import odscommon.service.GlowFilterService;
import odscommon.service.Test;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;



import be.ugent.ods.osgi.felix.FelixManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class OSGIMainActivity extends Activity {
	private String source = "local";
	private ToggleButton local;
	private ToggleButton dosgi;
	private ToggleButton rosgi;
	private Button testTestButton;
	private FelixManager felixManager;
	private ProgressDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String packageRootPath = getFilesDir().getAbsolutePath();
		 felixManager = new FelixManager(packageRootPath);
		setButtons();
		setTestbuttons();
	}

	private void setTestbuttons() {
		testTestButton = (Button) findViewById(R.id.button_runtest);
		testTestButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mProgressDialog = new ProgressDialog(OSGIMainActivity.this);
				mProgressDialog.setTitle("working");
				mProgressDialog.show();
				Thread thread = new Thread() {

					public void run() {
						// mColors = filter.filter(mColors, width, height);
						ServiceReference[] refs;
						String name = "";
						try {
							refs = felixManager.getFelix().getBundleContext().getServiceReferences(
									Test.class.getName(), "(type=test"+source+")");
							Test test = (Test) felixManager.getFelix().getBundleContext().getService(refs[0]);

							 name = test.echoLocation("myName");
						} catch (InvalidSyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						
						// mColors = filter.filter(mColors, width, height);
						final String response = name;
						OSGIMainActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								TextView text = (TextView) findViewById(R.id.textView_text);
								text.setText("answer was:"+response);
							}
						});
						mProgressDialog.dismiss();
					}
				};
				thread.setDaemon(true);
				thread.start();
				
			}
		});
		
	}

	private void setButtons() {
		local = (ToggleButton) findViewById(R.id.toggleButton_local);
		rosgi = (ToggleButton) findViewById(R.id.toggleButton_rosgi);
		dosgi = (ToggleButton) findViewById(R.id.toggleButton_dosgi);
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

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);

		return true;
	}
}
