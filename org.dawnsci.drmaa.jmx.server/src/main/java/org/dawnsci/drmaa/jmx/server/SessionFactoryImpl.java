/*
 * Copyright 2014 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dawnsci.drmaa.jmx.server;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

public class SessionFactoryImpl implements SessionFactoryMXBean {
  
  static final String SESSIONFACTORY_MXBEAN_NAME = "org.dawnsci.drmaa:type=SessionFactory";
  static final String SESSION_MXBEAN_NAME = "org.dawnsci.drmaa:type=Session";

  private SessionFactory localSessionFactory;
  private Session localSession;
  
  private SessionImpl remoteSession;

  private MBeanServer mbeanServer;
  private ObjectName sessionFactoryMxbeanName;
  private ObjectName sessionMxbeanName;

  public void setLocalSessionFactory(SessionFactory localSessionFactory) {
    this.localSessionFactory = localSessionFactory;
  }

  public void activate() throws Exception {
    mbeanServer = ManagementFactory.getPlatformMBeanServer();
    sessionFactoryMxbeanName = new ObjectName(SESSIONFACTORY_MXBEAN_NAME);
    mbeanServer.registerMBean(this, sessionFactoryMxbeanName);
  }

  public void deactivate() throws Exception {
    if (mbeanServer != null) {
      if(sessionFactoryMxbeanName!=null)
        mbeanServer.unregisterMBean(sessionFactoryMxbeanName);
      if(sessionMxbeanName!=null)
        mbeanServer.unregisterMBean(sessionMxbeanName);
    }
    if (localSession != null) {
      localSession.exit();
    }
  }

  @Override
  public SessionMXBean getSession() throws Exception {
    synchronized (this) {
      if (remoteSession == null) {
        remoteSession = new SessionImpl();
        sessionMxbeanName = new ObjectName(SESSION_MXBEAN_NAME);
        mbeanServer.registerMBean(remoteSession, sessionMxbeanName);
      }
    }
    return remoteSession;
  }
}
