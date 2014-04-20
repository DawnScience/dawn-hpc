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

import org.dawnsci.drmaa.jmx.SessionFactoryMXBean;
import org.dawnsci.drmaa.jmx.SessionMXBean;
import org.dawnsci.drmaa.jmx.connection.ConnectionManager;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author erwindl
 *
 */
public class SessionFactoryImpl implements SessionFactoryMXBean {
  private final static Logger LOGGER = LoggerFactory.getLogger(SessionFactoryImpl.class);

  private SessionFactory localSessionFactory;
  private Session localSession;

  private SessionImpl remoteSession;
  
  private ConnectionManager connMgr;

  public void setLocalSessionFactory(SessionFactory localSessionFactory) {
    LOGGER.debug("Set local DRMAA SessionFactory {}", localSessionFactory);
    this.localSessionFactory = localSessionFactory;
  }

  public void activate() throws Exception {
    LOGGER.trace("activate() - entry");
    connMgr = new ConnectionManager();
    connMgr.startSessionFactoryMXBean(this);
    LOGGER.trace("activate() - exit");
  }

  public void deactivate() throws Exception {
    LOGGER.trace("deactivate() - entry");
    if(connMgr!=null) {
      connMgr.stopSessionFactoryMXBean(this);
    }
    if (remoteSession != null) {
      try {
        remoteSession.exit();
      } catch (Exception e) {
        LOGGER.error("", e);
      }
      remoteSession = null;
    }
    if (localSession != null) {
      localSession.exit();
    }
    LOGGER.trace("deactivate() - exit");
  }

  @Override
  public SessionMXBean createSession() throws Exception {
    LOGGER.trace("createSession() - entry");
    synchronized (this) {
      if (localSession == null && localSessionFactory != null) {
        localSession = localSessionFactory.getSession();
        LOGGER.info("Created local DRMAA Session {}", localSession);
      }
      if (remoteSession == null) {
        remoteSession = new SessionImpl(localSession);
        remoteSession.registerMXBean();
      } else if (remoteSession.getLocalSession() == null && localSession != null) {
        remoteSession.setLocalSession(localSession);
      }
    }
    LOGGER.trace("createSession() - exit");
    return remoteSession;
  }
}
