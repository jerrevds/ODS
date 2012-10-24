package com.jabistudio.androidjhlabs.blurringandsharpeningactivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import odscommon.service.GlowFilterService;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;



import com.jabistudio.androidjhlabs.SuperFilterActivity;
import com.jabistudio.androidjhlabs.felix.FelixManager;
import com.jabistudio.androidjhlabs.felix.HostActivator;
import com.jabistudio.androidjhlabs.filter.DespeckleFilter;
import com.jabistudio.androidjhlabs.filter.GaussianFilter;
import com.jabistudio.androidjhlabs.filter.GlowFilter;
import com.jabistudio.androidjhlabs.filter.HighPassFilter;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class GlowFilterActivity extends SuperFilterActivity implements
		OnSeekBarChangeListener {
	private static final String TITLE = "Glow";

	private static final String RADIUS_STRING = "RADIUS:";
	private static final String AMOUNT_STRING = "AMOUNT:";
	private static final int MAX_VALUE = 100;

	private static final int RADIUS_SEEKBAR_RESID = 21865;
	private static final int AMOUNT_SEEKBAR_RESID = 21866;
	private String type;
	private SeekBar mRadiusSeekBar;
	private TextView mRadiusTextView;
	private SeekBar mAmountSeekBar;
	private TextView mAmountTextView;

	private int mRadiusValue;
	private int mAmountValue;

	private ProgressDialog mProgressDialog;
	private int[] mColors;
	/**
	 * 
	 * felix
	 */
	private HostActivator m_activator = null;
	private Felix m_felix = null;

	private FelixManager felixManager;

	private Button localButton;

	private Button rosgiButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(TITLE);
		
		filterSeekBarSetup(mMainLayout);
		// Create a configuration property map.
		
		String packageRootPath = getFilesDir().getAbsolutePath();
		 felixManager = new FelixManager(packageRootPath);
	/*	Map config = new HashMap();
		// Create host activator;
		m_activator = new HostActivator();
		List list = new ArrayList();
		list.add(m_activator);
		config.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);

		try {
			// Now create an instance of the framework with
			// our configuration properties.
			m_felix = new Felix(config);
			// Now start Felix instance.
			m_felix.start();
		} catch (Exception ex) {
			System.err.println("Could not create framework: " + ex);
			ex.printStackTrace();
		}*/
	}

	/**
	 * filterButtonSetting
	 * 
	 * @param mainLayout
	 */
	private void filterSeekBarSetup(LinearLayout mainLayout) {
	/*	mRadiusTextView = new TextView(this);
		mRadiusTextView.setText(RADIUS_STRING + mRadiusValue);
		mRadiusTextView.setTextSize(TITLE_TEXT_SIZE);
		mRadiusTextView.setTextColor(Color.BLACK);
		mRadiusTextView.setGravity(Gravity.CENTER);

		mRadiusSeekBar = new SeekBar(this);
		mRadiusSeekBar.setOnSeekBarChangeListener(this);
		mRadiusSeekBar.setId(RADIUS_SEEKBAR_RESID);
		mRadiusSeekBar.setMax(MAX_VALUE);

		mAmountTextView = new TextView(this);
		mAmountTextView.setText(AMOUNT_STRING + mAmountValue);
		mAmountTextView.setTextSize(TITLE_TEXT_SIZE);
		mAmountTextView.setTextColor(Color.BLACK);
		mAmountTextView.setGravity(Gravity.CENTER);

		mAmountSeekBar = new SeekBar(this);
		mAmountSeekBar.setOnSeekBarChangeListener(this);
		mAmountSeekBar.setId(AMOUNT_SEEKBAR_RESID);
		mAmountSeekBar.setMax(MAX_VALUE);*/

		LinearLayout buttons = new LinearLayout(this);
		buttons.setOrientation(LinearLayout.HORIZONTAL);
		localButton = new Button(this);
		this.localButton.setText("local");
		localButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				type="(type=glow)";
				mRadiusValue=56;
				mAmountValue=64;
				startOSGi();
				
			}

			
		});
		rosgiButton = new Button(this);
		this.rosgiButton.setText("rosgi");
		rosgiButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				type="(type=rglow)";
				mRadiusValue=56;
				mAmountValue=64;
				startOSGi();
				
			}
		});
		buttons.addView(localButton);
		
		buttons.addView(rosgiButton);
		mainLayout.addView(buttons);
	/*	mainLayout.addView(mAmountTextView);
		mainLayout.addView(mAmountSeekBar);
		mainLayout.addView(mRadiusTextView);
		mainLayout.addView(mRadiusSeekBar);*/
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
/*		switch (seekBar.getId()) {
		case AMOUNT_SEEKBAR_RESID:
			mAmountValue = progress;
			mAmountTextView.setText(AMOUNT_STRING + getAmout(mAmountValue));
			break;
		case RADIUS_SEEKBAR_RESID:
			mRadiusValue = progress;
			mRadiusTextView.setText(RADIUS_STRING + mRadiusValue);
			break;
		}*/
	}

	
	private void startOSGi() {
		final int width = mOriginalImageView.getDrawable().getIntrinsicWidth();
		final int height = mOriginalImageView.getDrawable()
				.getIntrinsicHeight();

		mColors = AndroidUtils.drawableToIntArray(mOriginalImageView
				.getDrawable());
		mProgressDialog = ProgressDialog.show(this, "", "Wait......");

		Thread thread = new Thread() {

			public void run() {
				// mColors = filter.filter(mColors, width, height);
				ServiceReference[] refs;

				try {
					refs = felixManager.getFelix().getBundleContext().getServiceReferences(
							GlowFilterService.class.getName(), type);
					GlowFilterService glow = (GlowFilterService) felixManager.getFelix().getBundleContext().getService(refs[0]);

					glow.setAmount(getAmout(mAmountValue));
					glow.setRadius(mRadiusValue);
					mColors = glow.filter(mColors, width, height);
				} catch (InvalidSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// GlowFilter filter = new GlowFilter();
				// Amount is 0~1 value
				// filter.setAmount(getAmout(mAmountValue));
				// filter.setRadius(mRadiusValue);

				// mColors = filter.filter(mColors, width, height);
				GlowFilterActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setModifyView(mColors, width, height);
					}
				});
				mProgressDialog.dismiss();
			}
		};
		thread.setDaemon(true);
		thread.start();
		
	}
	
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	/*	final int width = mOriginalImageView.getDrawable().getIntrinsicWidth();
		final int height = mOriginalImageView.getDrawable()
				.getIntrinsicHeight();

		mColors = AndroidUtils.drawableToIntArray(mOriginalImageView
				.getDrawable());
		mProgressDialog = ProgressDialog.show(this, "", "Wait......");

		Thread thread = new Thread() {

			public void run() {
				// mColors = filter.filter(mColors, width, height);
				ServiceReference[] refs;

				try {
					refs = felixManager.getFelix().getBundleContext().getServiceReferences(
							GlowFilterService.class.getName(), type);
					GlowFilterService glow = (GlowFilterService) felixManager.getFelix().getBundleContext().getService(refs[0]);

					glow.setAmount(getAmout(mAmountValue));
					glow.setRadius(mRadiusValue);
					mColors = glow.filter(mColors, width, height);
				} catch (InvalidSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// GlowFilter filter = new GlowFilter();
				// Amount is 0~1 value
				// filter.setAmount(getAmout(mAmountValue));
				// filter.setRadius(mRadiusValue);

				// mColors = filter.filter(mColors, width, height);
				GlowFilterActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setModifyView(mColors, width, height);
					}
				});
				mProgressDialog.dismiss();
			}
		};
		thread.setDaemon(true);
		thread.start();*/
	}

	private float getAmout(int value) {
		float retValue = 0;
		retValue = (float) (value / 100f);
		return retValue;
	}

	@Override
	protected void onDestroy() {
	
		felixManager.stopFelix();

		super.onDestroy();
	}
}
