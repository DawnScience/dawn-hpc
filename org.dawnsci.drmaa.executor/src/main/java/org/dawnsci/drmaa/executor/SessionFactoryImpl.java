/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.drmaa.executor;

import org.ggf.drmaa.SessionFactory;

/**
 * A SessionFactory implementation that executes jobs on a JDK ExecutorService.
 * 
 * @author erwindl
 *
 */
public class SessionFactoryImpl extends SessionFactory {
  private SessionImpl session;

  public void activate() {
    SessionFactory.setFactory(this);
  }

  public void deactivate() {
    synchronized (SessionFactory.class) {
      if (this.equals(SessionFactory.getFactory())) {
        SessionFactory.setFactory(null);
      }
    }
  }

  public SessionImpl getSession() {
    synchronized (this) {
      if (session == null) {
        session = new SessionImpl(3);
      }
    }
    return session;
  }
}
