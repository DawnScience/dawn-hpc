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
package org.dawnsci.passerelle.cluster.actor.internal;

import org.dawnsci.passerelle.cluster.service.IWorkflowClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple proxy to allow accessing a Workflow Cluster Service from inside actors etc.
 * <p>
 * When a IWorkflowClusterService is registered as an OSGi service, 
 * this holder will provide a static instance to access it.
 * </p>
 * 
 * @author erwindl
 * 
 */
public class WorkflowServiceHolder {
  private final static Logger LOGGER = LoggerFactory.getLogger(WorkflowServiceHolder.class);

  private static WorkflowServiceHolder instance = new WorkflowServiceHolder();

  private IWorkflowClusterService clusterService;

  public static WorkflowServiceHolder getInstance() {
    return instance;
  }

  /**
   * 
   * @return the IWorkflowClusterService that was found as a registered service
   * @throws IllegalStateException if no IWorkflowClusterService is available
   */
  public IWorkflowClusterService getClusterService() throws IllegalStateException {
    if (clusterService == null) {
      throw new IllegalStateException("No Workflow Cluster Service available");
    } else {
      return clusterService;
    }
  }

  public void setClusterService(IWorkflowClusterService workflowClusterService) {
    LOGGER.info("Setting Workflow Cluster Service : {}", workflowClusterService);
    this.clusterService = workflowClusterService;
    if (workflowClusterService != null) {
      instance = this;
    }
  }

  public void unsetClusterService(IWorkflowClusterService sessionFactory) {
    LOGGER.info("Unsetting Workflow Cluster Service");
    this.clusterService = null;
  }
}
