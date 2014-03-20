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
package org.dawnsci.drmaa.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.dawnsci.drmaa.common.JobTemplateImpl;
import org.dawnsci.drmaa.executor.impl.JobExecutionFuture;
import org.dawnsci.drmaa.executor.impl.JobExecutionTask;
import org.dawnsci.drmaa.executor.impl.JobExecutor;
import org.ggf.drmaa.AlreadyActiveSessionException;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.ExitTimeoutException;
import org.ggf.drmaa.InvalidJobException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.NoActiveSessionException;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Session implementation that executes jobs on a JDK ExecutorService.
 * 
 * @author erwindl
 * 
 */
public class SessionImpl implements Session {
  private final static Logger LOGGER = LoggerFactory.getLogger(SessionImpl.class);

  private boolean initialized = false;
  private String contact = "localhost";

  private static Map<UUID, JobTemplate> jobTemplates = new HashMap<>();
  private Map<String, JobExecutionFuture> jobExecutors = new HashMap<>();

  private ExecutorService jobExecutorService;

  public SessionImpl(int maxConcurrentJobs) {
    jobExecutorService = new JobExecutor(maxConcurrentJobs, maxConcurrentJobs, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
  }

  @Override
  public void init(String contact) throws DrmaaException {
    if (initialized) {
      throw new AlreadyActiveSessionException();
    } else {
      LOGGER.info("Initializing DRMAA Executor-based Session with contact {}", contact);
      initialized = true;
      this.contact = contact;
    }
  }

  @Override
  public void exit() throws DrmaaException {
    if (!initialized) {
      throw new NoActiveSessionException();
    } else {
      LOGGER.info("Exiting DRMAA Executor-based Session with contact {}", contact);
      initialized = false;
    }
  }

  @Override
  public JobTemplate createJobTemplate() throws DrmaaException {
    if (!initialized) {
      throw new NoActiveSessionException();
    } else {
      JobTemplateImpl jobTemplate = new JobTemplateImpl(UUID.randomUUID());
      jobTemplates.put(jobTemplate.getId(), jobTemplate);
      LOGGER.debug("createJobTemplate - created {}", jobTemplate);
      return jobTemplate;
    }
  }

  @Override
  public void deleteJobTemplate(JobTemplate jt) throws DrmaaException {
    if (!initialized) {
      throw new NoActiveSessionException();
    } else {
      JobTemplate removedJT = jobTemplates.remove(((JobTemplateImpl) jt).getId());
      if (removedJT != null) {
        LOGGER.debug("deleteJobTemplate - deleted {}", removedJT);
      } else {
        LOGGER.debug("deleteJobTemplate - template {} not found", jt);
      }
    }
  }

  @Override
  public String runJob(JobTemplate jt) throws DrmaaException {
    if (!initialized) {
      throw new NoActiveSessionException();
    } else {
      JobExecutionTask jet = new JobExecutionTask((JobTemplateImpl) jt);
      JobExecutionFuture jef = (JobExecutionFuture) jobExecutorService.submit(jet);
      jobExecutors.put(jet.getId(), jef);
      LOGGER.debug("runJob - running {}", jet);
      return jet.getId();
    }
  }

  @Override
  public List<String> runBulkJobs(JobTemplate jt, int start, int end, int incr) throws DrmaaException {
    if (!initialized) {
      throw new NoActiveSessionException();
    } else {
      List<String> results = new ArrayList<>();
      for (int i = start; i < end; i += incr) {
        JobExecutionTask jet = new JobExecutionTask((JobTemplateImpl) jt, i);
        JobExecutionFuture jef = (JobExecutionFuture) jobExecutorService.submit(jet);
        jobExecutors.put(jet.getId(), jef);
        results.add(jet.getId());
        LOGGER.debug("runBulkJobs - running {}", jet);
      }
      return results;
    }
  }

  @Override
  public void control(String jobId, int action) throws DrmaaException {
    // TODO Auto-generated method stub
  }

  @Override
  public void synchronize(List<String> jobIds, long timeout, boolean dispose) throws DrmaaException {
    if (!initialized) {
      throw new NoActiveSessionException();
    } else {
      Collection<String> actualJobIds = jobIds;
      if (jobIds.size() == 1 && JOB_IDS_SESSION_ALL.equals(jobIds.get(0))) {
        actualJobIds = jobExecutors.keySet();
      }
      for (String jobId : actualJobIds) {
        JobExecutionFuture jef = jobExecutors.get(jobId);
        if (jef != null) {
          try {
            jef.get(timeout, TimeUnit.SECONDS);
          } catch (CancellationException e) {
            // ignore, fact that job was cancelled should be reflected in JobInfo
          } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (TimeoutException e) {
            throw new ExitTimeoutException("Timeout waiting for " + jef.getId());
          }
        } else {
          throw new InvalidJobException("Job not found for id " + jobId);
        }
      }
    }
  }

  @Override
  public JobInfo wait(String jobId, long timeout) throws DrmaaException {
    if (!initialized) {
      throw new NoActiveSessionException();
    } else {
      String actualJobId = jobId;
      if (JOB_IDS_SESSION_ANY.equals(jobId) && !jobExecutors.isEmpty()) {
        actualJobId = jobExecutors.keySet().iterator().next();
      }
      JobExecutionFuture jef = jobExecutors.get(actualJobId);
      if (jef != null) {
        try {
          jef.get(timeout, TimeUnit.SECONDS);
        } catch (CancellationException e) {
          // ignore, fact that job was cancelled should be reflected in JobInfo
        } catch (InterruptedException | ExecutionException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (TimeoutException e) {
          throw new ExitTimeoutException("Timeout waiting for " + jef.getId());
        }
      } else {
        throw new InvalidJobException("Job not found for id " + actualJobId);
      }
      return null;
    }
  }

  @Override
  public int getJobProgramStatus(String jobId) throws DrmaaException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getContact() {
    return contact;
  }

  @Override
  public Version getVersion() {
    return new Version(1, 0);
  }

  @Override
  public String getDrmSystem() {
    return "DAWNSCI local Executor DRM";
  }

  @Override
  public String getDrmaaImplementation() {
    return "org.dawnsci.drmaa.executor";
  }
}
