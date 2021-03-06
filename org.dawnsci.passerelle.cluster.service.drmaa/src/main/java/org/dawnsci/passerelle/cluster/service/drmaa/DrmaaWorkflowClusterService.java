/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.passerelle.cluster.service.drmaa;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.dawnsci.passerelle.cluster.service.AnalysisJobBean;
import org.dawnsci.passerelle.cluster.service.IWorkflowClusterService;
import org.dawnsci.passerelle.cluster.service.JobListener;
import org.dawnsci.passerelle.cluster.service.JobRefusedException;
import org.dawnsci.passerelle.cluster.service.SliceBean;
import org.dawnsci.passerelle.cluster.service.drmaa.internal.ClusterServiceConfigurer;
import org.dawnsci.passerelle.cluster.service.drmaa.internal.DrmaaJobWaiterService;
import org.ggf.drmaa.AlreadyActiveSessionException;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.NoActiveSessionException;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author erwindl
 * 
 */
public class DrmaaWorkflowClusterService implements IWorkflowClusterService {
  private final static Logger LOGGER = LoggerFactory.getLogger(DrmaaWorkflowClusterService.class);

  private boolean active;

  /**
   * The DRMAA session factory
   */
  private SessionFactory sessionFactory;
  private Session session;

  private ClusterServiceConfigurer configurer;
  private DrmaaJobWaiterService jobWaiterService;

  public void setSessionFactory(SessionFactory sessionFactory) {
    LOGGER.info("Setting DRMAA SessionFactory : {}", sessionFactory);
    this.sessionFactory = sessionFactory;
  }

  public void unsetSessionFactory(SessionFactory sessionFactory) {
    LOGGER.info("Unsetting DRMAA SessionFactory");
    this.sessionFactory = null;
  }

  public void activate() throws Exception {
    LOGGER.trace("activate() - entry");
    try {
      session = sessionFactory.getSession();
      session.init(null);
      LOGGER.info("DrmaaWorkflowClusterService activated");
    } catch (AlreadyActiveSessionException e) {
      // ignore as this could just imply that DRMAA is already ready for us
    }
    jobWaiterService = new DrmaaJobWaiterService(session);
    configurer = new ClusterServiceConfigurer();

    active = true;
    LOGGER.trace("activate() - exit");
  }

  public void deactivate() throws Exception {
    LOGGER.trace("deactivate() - entry");
    active = false;
    
    if (jobWaiterService != null) {
      jobWaiterService.shutDown();
    }
    if (session != null) {
      try {
        session.exit();
      } catch (NoActiveSessionException e) {
        // ignore as this could just imply that the exit was not required
      }
      session = null;
    }
    LOGGER.info("DrmaaWorkflowClusterService deactivated");
    LOGGER.trace("deactivate() - exit");
  }

  /**
   * extraArgs will be written as properties in the jobSpec file, next to the slice elements
   */
  @Override
  public AnalysisJobBean submitAnalysisJob(String initiator, String correlationID, String runtimeSpec, String workflowSpec, SliceBean dataSpec, 
      Map<String, String> extraArgs, long timeout, TimeUnit unit, JobListener listener) throws JobRefusedException {
    
    LOGGER.trace("submitAnalysisJob() - entry : {} submits job {} with runtime {} and workflow {} for data {}", 
        new Object[] { initiator, correlationID, runtimeSpec, workflowSpec, dataSpec });
    if (!active) {
      throw new JobRefusedException("DrmaaWorkflowClusterService not active");
    }
    String dataFile = dataSpec.getFilePath();
    File processingRootFolder = configurer.getProcessingRootForCollectedData(new File(dataFile), null);
    File jobFolder = configurer.getNewProcessingJobFolder(processingRootFolder );
    writeSliceBean(jobFolder, dataSpec, extraArgs);
    
    AnalysisJobBean jobBean = new AnalysisJobBean(initiator, jobFolder, correlationID, dataSpec);
    try {
      JobTemplate jobTemplate = session.createJobTemplate();
      String[] remoteCommandAndArgs = runtimeSpec.split(" ");
      jobTemplate.setRemoteCommand(remoteCommandAndArgs[0]);
      List<String> args = new ArrayList<>();
      // these are the standard required arguments tightly linked to the remote command
      // as defined for the current clustering environment
      for (int i = 1; i < remoteCommandAndArgs.length; ++i) {
        args.add(remoteCommandAndArgs[i]);
      }
      // these are the "real" job-specific arguments
      args.add(workflowSpec);
      args.add("workingDir=" + jobFolder.getAbsolutePath());
      jobTemplate.setArgs(args);
      jobTemplate.setWorkingDirectory(jobFolder.getAbsolutePath());
      
      if(extraArgs!=null && extraArgs.containsKey("jobCategory")) {
        jobTemplate.setJobCategory(extraArgs.get("jobCategory"));
      }
      if(extraArgs!=null && extraArgs.containsKey("nativeSpec")) {
        jobTemplate.setNativeSpecification(extraArgs.get("nativeSpec"));
      }
      
      String drmaaJobId = session.runJob(jobTemplate);
      jobBean.setInternalJobID(drmaaJobId);

      jobWaiterService.submitjob(jobBean, listener, timeout, unit);

      session.deleteJobTemplate(jobTemplate);
      LOGGER.info("Submitted job {}", jobBean);
      LOGGER.trace("submitAnalysisJob() - exit");
      return jobBean;
    } catch (DrmaaException e) {
      throw new JobRefusedException("Error submitting job", e);
    }
  }

  private void writeSliceBean(File jobFolder, SliceBean sliceBean, Map<String,String> extraArgs) {
    Properties props = sliceBean.toProperties();
    if(extraArgs!=null && !extraArgs.isEmpty()) {
      for(String argKey : extraArgs.keySet()) {
        props.put(argKey, extraArgs.get(argKey));
      }
    }
    Writer writer = null;
    try {
      writer = new FileWriter(new File(jobFolder, "dataSlice.properties"));
      props.store(writer, null);
    } catch (Exception e) {
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (Exception e) {
        }
      }
    }
  }
}
