package be.ugent.ods.osgi.tests.implementations;

import android.widget.TextView;
import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;
import be.ugent.ods.osgi.tests.interfaces.FeedbackInterface;
import be.ugent.ods.osgi.tests.interfaces.TestInterface;
import be.ugent.ods.testapplications.service.interfaces.EchoService;

public class EchoTest implements TestInterface {
	
	public void runTest(ModuleAccessor accessor, FeedbackInterface feedback) {
		
		EchoService echoservice = accessor.getModule(EchoService.class);
		
		String response = echoservice.echoString("print this!");
		
		//show the anwser
		TextView text = new TextView(feedback.getActivity());
		text.setText("answer was: "+response);
		feedback.pushTestView(text);
	}

}
