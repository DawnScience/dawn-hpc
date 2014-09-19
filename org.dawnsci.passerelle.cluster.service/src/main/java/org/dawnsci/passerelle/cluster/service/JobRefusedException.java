/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.passerelle.cluster.service;


/**
 * A very simple exception impl, not extending PasserellException 
 * as we don't want to depend on ptolemy etc in this service layer.
 * 
 * @author erwindl
 *
 */
public class JobRefusedException extends Exception {
  private static final long serialVersionUID = 813137060408229630L;

  public JobRefusedException(String message, Throwable cause) {
    super(message, cause);
  }

  public JobRefusedException(String message) {
    super(message);
  }
}
