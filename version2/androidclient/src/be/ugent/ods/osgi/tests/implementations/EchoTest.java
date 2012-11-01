package be.ugent.ods.osgi.tests.implementations;

import android.content.Intent;
import android.widget.TextView;
import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;
import be.ugent.ods.osgi.tests.interfaces.FeedbackInterface;
import be.ugent.ods.osgi.tests.interfaces.TestInterface;
import be.ugent.ods.osgi.tests.measure.MeasurementTool;
import be.ugent.ods.testapplications.service.interfaces.EchoService;

public class EchoTest extends TestInterface {
	
	private String response;
	private EchoService echoservice;


	@Override
	public void runActivityForResult(int requestCode, int resultCode, Intent data) {
	}

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
		// TODO Auto-generated method stub
		return "echotest";
	}

}
