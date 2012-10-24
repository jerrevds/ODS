package com.jabistudio.androidjhlabs.felix;

import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;



public class HostActivator //implements BundleActivator
{
	/*
	    private BundleContext m_context = null;
	    private ServiceRegistration m_registration = null;

	    public HostActivator()
	    {
	       
	    }
	    
	    public BundleContext getContext(){
	    	return m_context;
	    	
	    }

	    public void start(BundleContext context)
	    {
	        // Save a reference to the bundle context.
	        m_context = context;
	        // Create a property lookup service implementation.
	        GlowFilterService glower = new GlowFilterServiceImpl();
	        // Register the property lookup service and save
	        // the service registration.
	        Hashtable<String, String> props = new Hashtable<String, String>();
	        props.put("type", "glow");
	        m_registration = m_context.registerService(
	        		GlowFilterService.class.getName(), glower, props);
	    }

	    public void stop(BundleContext context)
	    {
	        // Unregister the property lookup service.
	        m_registration.unregister();
	        m_context = null;
	    }*/
}