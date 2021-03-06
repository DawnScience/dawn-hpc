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
 * Minimal interface to identify a job and to obtain its exit code.
 * 
 * @author erwindl
 *
 */
public interface IJob {

  /**
   * 
   * @return the job's correlation ID as set by the application during its job submission
   */
  String getCorrelationID();

  /**
   * 
   * @return the Job ID as assigned internally by the cluster service
   */
  String getInternalJobID();
  
  /**
   * 
   * @return true if the job is done or still pending/ongoing.
   */
  boolean isFinished();
  
  /**
   * 
   * @return a code to know whether the execution went fine (0) or ended in error (some other code)
   */
  int getExitCode();
  
  /**
   * 
   * @return some extra message that may explain what went OK/wrong during the job's execution
   */
  String getExitMessage();
}
