package com.example.greetertest;

import greeter.api.GreeterInterface;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;

import osgi.OSGiRuntime;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class GreeterActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greeter);
        
        final Button button = (Button) findViewById(R.id.btnGreet);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText txtName = (EditText) findViewById(R.id.txtName);
                final String name = txtName.getText().toString();
            	
                // Perform action on click
            	AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>(){
 
        			@Override
					protected String doInBackground(String... params) {
						// Lookup Greeter Service with OSGi context
        				BundleContext context = OSGiRuntime.getOSGiRuntime().getBundleContext();
        				String result = "Greetings: \n";
						try {
							ServiceReference[] refs = context.getServiceReferences(GreeterInterface.class.getName(), null);
				    		if(refs!=null){
				    			for(ServiceReference ref : refs){
					    			try {
					    				GreeterInterface greeter = (GreeterInterface)context.getService(ref);
					    				result += greeter.greet(params[0])+"\n";
					    			} catch(Exception e){
					    				result += e.getMessage()+"\n";
					    			}
				    			}
				    		} 
						} catch (InvalidSyntaxException e1) {
							// should not happen, no filter specified
						}
        				
						return result;
					}
        			
        			@Override
            		protected void onPostExecute(String result) {
        		        final TextView txtName = (TextView) findViewById(R.id.txtResult);
        		        txtName.setText(result);
                	}
            	};
            	task.execute(name);
            }
        });
        
        final Button btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText txtEndpointId = (EditText) findViewById(R.id.txtEndpointID);
                final String endpointId = txtEndpointId.getText().toString();
            	
                // Perform action on click
            	AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>(){
 
        			@Override
					protected Void doInBackground(String... params) {
						// Lookup Greeter Service with OSGi context
        				BundleContext context = OSGiRuntime.getOSGiRuntime().getBundleContext();
        				String result = "Greetings: \n";
			
						ServiceReference ref = context.getServiceReference(RemoteServiceAdmin.class.getName());
			    		if(ref!=null){
			    			try {
			    				RemoteServiceAdmin rsa = (RemoteServiceAdmin)context.getService(ref);
			    				Map<String, Object> properties = new HashMap<String, Object>();
								properties.put("endpoint.id", endpointId);
								properties.put("service.imported.configs", "r-osgi");
								properties.put("objectClass", new String[]{GreeterInterface.class.getName()});
								EndpointDescription endpoint = new EndpointDescription(properties);
								ImportRegistration ir = rsa.importService(endpoint);
			    			} catch(Exception e){
			    				e.printStackTrace();
			    			}
			    		} 
						return null;
					}
            	};
            	task.execute(endpointId);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_greeter, menu);
        return true;
    }
    
    @Override
    protected void onStart(){
    	super.onStart();
    	// start OSGi runtime and the bundles
    	OSGiRuntime osgi = OSGiRuntime.getOSGiRuntime();

		if(!osgi.isStarted()){
			osgi.start();
			
			startBundle("rsa");
			startBundle("greeter_interface");
			startBundle("greeter_service");
		}
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		OSGiRuntime.getOSGiRuntime().stop();
		System.exit(0);
	}
    
    private void startBundle(String name) {
		System.out.println("Starting bundle " + name);
		int id = getResources().getIdentifier(name, "raw", "com.example.greetertest");
		InputStream is = getResources().openRawResource(id);
		try {
			org.osgi.framework.Bundle b = OSGiRuntime.getOSGiRuntime()
					.getBundleContext().installBundle(name + ".jar", is);
			b.start();
			System.out.println("Bundle " + name + " with id "+b.getBundleId()+" starting");
		} catch (Exception e) {
			System.out.println("Error starting bundle " + name);
			e.printStackTrace(System.out);
		}
	} 
}
