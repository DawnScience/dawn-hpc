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
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.dawnsci.passerelle.cluster.service.AnalysisJobBean;
import org.dawnsci.passerelle.cluster.service.IWorkflowClusterService;
import org.dawnsci.passerelle.cluster.service.JobListener;
import org.dawnsci.passerelle.cluster.service.JobRefusedException;
import org.dawnsci.passerelle.cluster.service.SliceBean;
import org.dawnsci.passerelle.cluster.service.drmaa.internal.DrmaaJobWaiterService;
import org.dawnsci.passerelle.cluster.service.drmaa.internal.DrmaaSessionFactoryHolder;
import org.ggf.drmaa.AlreadyActiveSessionException;
import org.ggf.drmaa.DrmaaException;
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
  // TODO point to real DAWN cluster node runtimes && shared job folder
  private static final String JOB_RUNTIME_JAR = "C:/temp/dls_trials/bin/PasserelleRuntime.jar";
  private static final String JOB_FOLDER = "C:/temp/dls_trials/jobs";

  private final static Logger LOGGER = LoggerFactory.getLogger(DrmaaWorkflowClusterService.class);

  private boolean active;
  
  private volatile AtomicLong jobIdCounter;

  /**
   * The DRMAA session
   */
  private Session session;

  private DrmaaJobWaiterService jobWaiterService;
  private DrmaaSessionFactoryHolder sessFactoryHolder;

  public void setSessionFactoryHolder(DrmaaSessionFactoryHolder sessFactoryHolder) {
    this.sessFactoryHolder = sessFactoryHolder;
  }

  public void unsetSessionFactoryHolder(DrmaaSessionFactoryHolder sessFactoryHolder) {
    this.sessFactoryHolder = null;
  }

  public void activate() throws Exception {
    LOGGER.trace("activate() - entry");
    try {
      session = sessFactoryHolder.getSessionFactory().getSession();
      session.init(null);
      LOGGER.info("DrmaaWorkflowClusterService activated");
    } catch (AlreadyActiveSessionException e) {
      // ignore as this could just imply that DRMAA is already ready for us
    }
    jobWaiterService = new DrmaaJobWaiterService(session, JOB_FOLDER);
    Properties metaData = readMetaData();
    if(metaData!=null && metaData.containsKey("jobID")) {
      String jobIDCtrStart = metaData.getProperty("jobID");
      jobIdCounter = new AtomicLong(Long.parseLong(jobIDCtrStart)+1);
    }
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
    // TODO write last jobID in metadata
    LOGGER.info("DrmaaWorkflowClusterService deactivated");
    LOGGER.trace("deactivate() - exit");
  }

  @Override
  public AnalysisJobBean submitAnalysisJob(String initiator, String correlationID, String workflowSpec, SliceBean dataSpec, long timeout, TimeUnit unit,
      JobListener listener) throws JobRefusedException {
    LOGGER.trace("submitAnalysisJob() - entry : {} submits job {} with workflow {} for data {}", 
        new Object[] { initiator, correlationID, workflowSpec, dataSpec });
    if (!active) {
      throw new JobRefusedException("DrmaaWorkflowClusterService not active");
    }
    AnalysisJobBean jobBean = new AnalysisJobBean(initiator, jobIdCounter.getAndIncrement(), correlationID, dataSpec);
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
      args.add("dataSlice=" + dataSpec.getSlice());
      String jobFolderStr = JOB_FOLDER + File.separatorChar + initiator + File.separatorChar + jobBean.getJobID();
      File jobFolder = new File(jobFolderStr);
      if(!jobFolder.exists()) {
        jobFolder.mkdirs();
      }
      args.add("jobFolder=" + jobFolderStr);
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

  /**
   * @return metadata props for the cluster jobs; returns empty properties when no metadata is found
   */
  private Properties readMetaData() {
    Properties jobsMetaDataProps = new Properties();
    File metaDataFile = new File(JOB_FOLDER, ".metadata");
    if (metaDataFile.exists()) {
      Reader metaDataReader = null;
      try {
        metaDataReader = new FileReader(metaDataFile);
        jobsMetaDataProps.load(metaDataReader);
      } catch (Exception e) {

      } finally {
        if (metaDataReader != null) {
          try {
            metaDataReader.close();
          } catch (Exception e) {
          }
        }
      }
    }
    return jobsMetaDataProps;
  }
}
