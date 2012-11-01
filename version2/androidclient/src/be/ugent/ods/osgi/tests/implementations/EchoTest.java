package be.ugent.ods.osgi.tests.implementations;

import android.widget.TextView;
import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;
import be.ugent.ods.osgi.tests.interfaces.AbstractTest;
import be.ugent.ods.testapplications.service.interfaces.EchoService;

public class EchoTest extends AbstractTest {
	
	private String response;
	private EchoService echoservice;

	@Override
	public void test() {
		response = echoservice.echoString("print this!");
		
	}

	@Override
	public void preRun(ModuleAccessor accessor) {
		echoservice = accessor.getModule(EchoService.class);
	}

	@Override
	public void postRun() {
		TextView text = new TextView(feedback.getActivity());
		text.setText("answer was: "+response);
		feedback.pushTestView(text);
	}

	@Override
	public String getName() {
		return "echotest";
	}

}
