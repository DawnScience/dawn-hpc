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

package org.dawnsci.passerelle.cluster.service;

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
   * @param correlationID an ID set by the application so it can correlate listener notifications to its original job submission
   * @param workflowSpec the name of the workflow to be used
   * @param dataSpec
   * @param timeout
   * @param unit
   * @param listener an optional listener that will be notified when the submitted job is finished (or failed to be executed)
   * @return an IJob for an analysis job, when the submission was OK.
   * 
   * @throws JobRefusedException when the job can not be accepted for whatever reason
   */
  AnalysisJobBean submitAnalysisJob(String correlationID, String workflowSpec, SliceBean dataSpec, long timeout, TimeUnit unit, JobListener listener) throws JobRefusedException;
}
