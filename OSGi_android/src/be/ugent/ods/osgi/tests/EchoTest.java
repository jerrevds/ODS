package be.ugent.ods.osgi.tests;

import odscommon.service.interfaces.EchoService;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import android.app.ProgressDialog;
import android.widget.TextView;
import be.ugent.ods.osgi.OSGIMainActivity;
import be.ugent.ods.osgi.felix.FelixManager;

public class EchoTest implements TestInterface {

	private FelixManager felixManager;
	

	private ProgressDialog mProgressDialog;
	
	
	

	public EchoTest(FelixManager felixManager) {
		super();
		this.felixManager = felixManager;
	}




	public void runTest(final String source, final OSGIMainActivity activity) {
		//new thread to avoid stalls of android
		Thread thread = new Thread() {

			public void run() {
				
				ServiceReference[] refs;
				String name = "";
				try {
					refs = felixManager.getFelix().getBundleContext().getServiceReferences(
							EchoService.class.getName(), "(type=test"+source+")");
					Object o = felixManager.getFelix().getBundleContext();
					EchoService test = (EchoService) felixManager.getFelix().getBundleContext().getService(refs[0]);

					 name = test.echoLocation("myName");
				} catch (InvalidSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				final String response = name;
				//push response back
				TextView text = new TextView(activity);
				text.setText("answer was:"+response);
				activity.pushTestView(text);

			}
		};
		thread.setDaemon(true);
		thread.start();
	}

}
