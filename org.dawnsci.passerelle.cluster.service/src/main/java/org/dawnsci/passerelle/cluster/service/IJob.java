/*
 * Copyright 2014 Diamond Light Source Ltd. and iSencia Belgium NV.
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
