/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.passerelle.cluster.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author erwindl
 *
 */
public interface IWorkflowClusterService {
  /**
   * Submits a cluster job to launch an analysis workflow on the specified data slice.
   * 
   * Remark that the job submission does not guarantee its execution.
   * Job status will be reported via the listener.
   * 
   * @param initiator the party/person/system initiating the job submission. E.g. a user ID.
   * @param correlationID an ID set by the application so it can correlate listener notifications to its original job submission
   * @param runtimeSpec the command to start the workflow runtime
   * @param workflowSpec the name of the workflow to be used
   * @param dataSpec
   * @param extraArgs extra key/value arguments to pass to the job
   * @param timeout
   * @param unit
   * @param listener an optional listener that will be notified when the submitted job is finished (or failed to be executed)
   * @return an IJob for an analysis job, when the submission was OK.
   * 
   * @throws JobRefusedException when the job can not be accepted for whatever reason
   */
  AnalysisJobBean submitAnalysisJob(String initiator, String correlationID, String runtimeSpec, String workflowSpec, SliceBean dataSpec, Map<String, String> extraArgs,
      long timeout, TimeUnit unit, JobListener listener) throws JobRefusedException;
}
