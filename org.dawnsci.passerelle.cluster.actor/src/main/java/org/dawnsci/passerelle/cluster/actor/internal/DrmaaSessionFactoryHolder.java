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
package org.dawnsci.passerelle.cluster.actor.internal;

import org.ggf.drmaa.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple proxy to allow accessing a DRMAA SessionFactory from inside actors etc.
 * <p>
 * When a DRMAA SessionFactory is registered as an OSGi service, 
 * this holder will provide a static instance to access it.
 * </p>
 * TODO implement and adapt to a configuration where multiple DRMAA SessionFactory services can be present,
 * in which case the one with the highest ranking should be used.
 * 
 * @author erwindl
 * 
 */
public class DrmaaSessionFactoryHolder {
  private final static Logger LOGGER = LoggerFactory.getLogger(DrmaaSessionFactoryHolder.class);

  private static DrmaaSessionFactoryHolder instance = new DrmaaSessionFactoryHolder();

  private SessionFactory sessionFactory;

  public static DrmaaSessionFactoryHolder getInstance() {
    return instance;
  }

  /**
   * 
   * @return the DRMAA SessionFactory that was found as a registered service
   * @throws IllegalStateException if no DRMAA SessionFactory is available
   */
  public SessionFactory getSessionFactory() throws IllegalStateException {
    if (sessionFactory == null) {
      throw new IllegalStateException("No DRMAA SessionFactory available");
    } else {
      return sessionFactory;
    }
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    LOGGER.info("Setting DRMAA SessionFactory : {}", sessionFactory);
    this.sessionFactory = sessionFactory;
    if (sessionFactory != null) {
      instance = this;
    }
  }

  public void unsetSessionFactory(SessionFactory sessionFactory) {
    LOGGER.info("Unsetting DRMAA SessionFactory");
    this.sessionFactory = null;
  }
}
