/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.drmaa.jmx;

/**
 * The interface for a JMX-based remote access to a DRMAA SessionFactory.
 * 
 * @author erwindl
 *
 */
public interface SessionFactoryMXBean {
  
  /**
   * In order to follow JMX conventions about getters&setters, 
   * the getSession method as defined in drmaa's SessionFactory
   * is renamed to createSession here.
   * 
   * @return
   * @throws Exception
   */
  SessionMXBean createSession() throws Exception;

}
