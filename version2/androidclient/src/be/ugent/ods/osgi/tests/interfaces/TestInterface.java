package be.ugent.ods.osgi.tests.interfaces;

import android.app.Activity;
import android.content.Intent;
import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;

public interface TestInterface {
	
	public void runTest(ModuleAccessor accessor, FeedbackInterface feedback);
	
	public void runActivityForResult(int requestCode, int resultCode, Intent data);
	
}
