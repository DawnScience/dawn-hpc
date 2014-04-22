/* Copyright 2013 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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
