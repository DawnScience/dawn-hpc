/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
