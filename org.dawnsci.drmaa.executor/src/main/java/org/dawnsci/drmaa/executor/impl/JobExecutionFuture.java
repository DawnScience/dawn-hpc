/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.drmaa.executor.impl;

import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author erwindl
 *
 */
public class JobExecutionFuture extends FutureTask<Integer> {
  private final static Logger LOGGER = LoggerFactory.getLogger(JobExecutionFuture.class);

  private JobExecutionTask jet;

  public JobExecutionFuture(JobExecutionTask jet) {
    super(jet);
    this.jet = jet;
  }
  
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    try {
      jet.cancel();
      LOGGER.info("Cancelled job "+jet);
    } catch (Throwable t) {
      LOGGER.error("Failed to cancel Job "+jet,t);
    }
    return super.cancel(mayInterruptIfRunning);
  }
  
  public String getId() {
    return jet.getId();
  }
}
