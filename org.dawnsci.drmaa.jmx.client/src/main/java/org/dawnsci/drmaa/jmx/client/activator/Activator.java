/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.drmaa.jmx.client.activator;

import java.util.Hashtable;

import org.dawnsci.drmaa.jmx.client.SessionFactoryImpl;
import org.dawnsci.drmaa.jmx.connection.ConnectionManager;
import org.ggf.drmaa.SessionFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use an activator i.o. DS to be able to prevent component instantiation and registration when JMX connection is not
 * available
 * 
 * @author erwindl
 * 
 */
public class Activator implements BundleActivator {
  private final static Logger LOGGER = LoggerFactory.getLogger(Activator.class);

  private ServiceRegistration<SessionFactory> sessFactorySvcReg;

  public void start(BundleContext context) throws Exception {
    SessionFactoryImpl sessFactorySvc = new SessionFactoryImpl();
    try {
      sessFactorySvc.activate();
      Hashtable<String, Object> svcProps = new Hashtable<String, Object>();
      svcProps.put("service.ranking", 100);
      svcProps.put("drmaa.type", "JMX Client");
      sessFactorySvcReg = (ServiceRegistration<SessionFactory>) context.registerService(SessionFactory.class, sessFactorySvc, svcProps);
    } catch (Exception e) {
      if(Boolean.getBoolean(ConnectionManager.DRMAA_JMX_CONNECTION_REQUIRED_PROPERTY_NAME)) {
        LOGGER.error("DRMAA JMX client connector activation failed " + e);
      } else {
        // no need to log an error
      }
    }
  }

  public void stop(BundleContext context) throws Exception {
    if (sessFactorySvcReg != null) {
      sessFactorySvcReg.unregister();
    }
  }
}
