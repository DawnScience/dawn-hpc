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

package org.dawnsci.passerelle.cluster.service.drmaa;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.dawnsci.passerelle.cluster.service.AnalysisJobBean;
import org.dawnsci.passerelle.cluster.service.IWorkflowClusterService;
import org.dawnsci.passerelle.cluster.service.JobListener;
import org.dawnsci.passerelle.cluster.service.JobRefusedException;
import org.dawnsci.passerelle.cluster.service.SliceBean;
import org.dawnsci.passerelle.cluster.service.drmaa.internal.DrmaaSessionFactoryHolder;
import org.dawnsci.passerelle.cluster.service.drmaa.internal.DrmaaJobWaiterService;
import org.ggf.drmaa.AlreadyActiveSessionException;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.NoActiveSessionException;
import org.ggf.drmaa.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author erwindl
 * 
 */
public class DrmaaWorkflowClusterService implements IWorkflowClusterService {
  private static final String JOB_RUNTIME_JAR = "C:/temp/dls_trials/bin/PasserelleRuntime.jar";

  private static final String JOB_OUTPUTFOLDER = "C:/temp/dls_trials/output";

  private final static Logger LOGGER = LoggerFactory.getLogger(DrmaaWorkflowClusterService.class);
  
  private boolean active;

  /**
   * The DRMAA session
   */
  private Session session;
  
  private DrmaaJobWaiterService jobWaiterService;

  public void activate() throws Exception {
    LOGGER.trace("activate() - entry");
    try {
      session = DrmaaSessionFactoryHolder.getInstance().getSessionFactory().getSession();
      session.init(null);
      LOGGER.info("DrmaaWorkflowClusterService activated");
    } catch (AlreadyActiveSessionException e) {
      // ignore as this could just imply that DRMAA is already ready for us
    }
    jobWaiterService = new DrmaaJobWaiterService(session, JOB_OUTPUTFOLDER);
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

  @Override
  public AnalysisJobBean submitAnalysisJob(String correlationID, String workflowSpec, SliceBean dataSpec, long timeout, TimeUnit unit, JobListener listener) throws JobRefusedException {
    LOGGER.trace("submitAnalysisJob() - entry : job {} with workflow {} for data {}", new Object[] { correlationID, workflowSpec, dataSpec });
    if(!active) {
      throw new JobRefusedException("DrmaaWorkflowClusterService not active");
    }
    AnalysisJobBean jobBean = new AnalysisJobBean(1L, correlationID, dataSpec);
    try {
      JobTemplate jobTemplate = session.createJobTemplate();
      jobTemplate.setRemoteCommand("java");
      List<String> args = new ArrayList<>();
      args.add("-jar");
      args.add(JOB_RUNTIME_JAR);
      args.add(workflowSpec);
      args.add("jobID=" + jobBean.getJobID());
      args.add("dataFile=" + dataSpec.getFilePath());
      args.add("dataSet=" + dataSpec.getDataSet());
      args.add("outputFolder=" + JOB_OUTPUTFOLDER);
      jobTemplate.setArgs(args);
      String drmaaJobId = session.runJob(jobTemplate);
      jobBean.setInternalJobID(drmaaJobId);

      jobWaiterService.submitjob(jobBean, listener, timeout, unit);

      LOGGER.info("Submitted job {}", jobBean);
      LOGGER.trace("submitAnalysisJob() - exit");
      return jobBean;
    } catch (DrmaaException e) {
      throw new JobRefusedException("Error submitting job", e);
    }
  }
}
