package be.ugent.ods.osgi.tests.interfaces;

import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;

public interface TestInterface {
	
	public void runTest(ModuleAccessor accessor, FeedbackInterface feedback);
	
}
