/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.dawnsci.drmaa.common.JobInfoImpl;
import org.dawnsci.drmaa.common.JobTemplateImpl;
import org.dawnsci.drmaa.executor.impl.JobExecutionFuture;
import org.dawnsci.drmaa.executor.impl.JobExecutionTask;
import org.dawnsci.drmaa.executor.impl.JobExecutor;
import org.ggf.drmaa.AlreadyActiveSessionException;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.ExitTimeoutException;
import org.ggf.drmaa.InvalidJobException;
import org.ggf.drmaa.InvalidJobTemplateException;
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
      for (JobTemplate removedJT : jobTemplates.values()) {
        deleteJobTemplate(removedJT);
      }
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
      validateJobTemplate(jt);
      JobExecutionTask jet = new JobExecutionTask((JobTemplateImpl) jt);
      JobExecutionFuture jef = (JobExecutionFuture) jobExecutorService.submit(jet);
      jobExecutors.put(jet.getId(), jef);
      LOGGER.debug("runJob - running {}", jet);
      return jet.getId();
    }
  }

  private void validateJobTemplate(JobTemplate jt) throws InvalidJobTemplateException {
    UUID jobId = ((JobTemplateImpl) jt).getId();
    if (jobId == null || !jobTemplates.containsKey(jobId)) {
      throw new InvalidJobTemplateException("Unknown JobTemplate ID " + jobId);
    }
  }

  @Override
  public List<String> runBulkJobs(JobTemplate jt, int start, int end, int incr) throws DrmaaException {
    if (!initialized) {
      throw new NoActiveSessionException();
    } else {
      validateJobTemplate(jt);
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
    throw new UnsupportedOperationException("control() action not implemented for executor-based DRMAA session");
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
            if (timeout >= 0) {
              // remark that is not exactly the right timeout behaviour
              // as we're potentially multiplying the total timeout with the nr of jobs
              jef.get(timeout, TimeUnit.SECONDS);
            } else {
              jef.get();
            }
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
      if (dispose) {
        jobExecutors.clear();
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
      JobExecutionFuture jef = jobExecutors.remove(actualJobId);
      int exitStatus = 0;
      if (jef != null) {
        try {
          if (timeout >= 0) {
            exitStatus = jef.get(timeout, TimeUnit.SECONDS);
          } else {
            exitStatus = jef.get();
          }
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
      return new JobInfoImpl(actualJobId, jef.isDone(), exitStatus, jef.isCancelled());
    }
  }

  @Override
  public int getJobProgramStatus(String jobId) throws DrmaaException {
    if (!initialized) {
      throw new NoActiveSessionException();
    } else {
      JobExecutionFuture jef = jobExecutors.get(jobId);
      if (jef != null) {
        if (!jef.isDone()) {
          return Session.RUNNING;
        } else if (jef.isCancelled()) {
          return Session.USER_SYSTEM_SUSPENDED;
        } else {
          try {
            int exitStatus = jef.get(1, TimeUnit.MILLISECONDS);
            return (exitStatus == 0) ? Session.DONE : Session.FAILED;
          } catch (ExecutionException e) {
            return Session.FAILED;
          } catch (InterruptedException | TimeoutException e) {
            return Session.RUNNING;
          }
        }
      } else {
        return Session.UNDETERMINED;
      }
    }
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
