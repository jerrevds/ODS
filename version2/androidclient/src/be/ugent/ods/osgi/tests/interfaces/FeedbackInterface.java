package be.ugent.ods.osgi.tests.interfaces;

import android.app.Activity;
import android.view.View;

public interface FeedbackInterface {
	
	public void pushTestView(final View view);
	
	public Activity getActivity();
}
