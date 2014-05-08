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

package org.dawnsci.passerelle.cluster.service.drmaa.internal;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.dawnsci.passerelle.cluster.service.AnalysisJobBean;
import org.dawnsci.passerelle.cluster.service.JobListener;
import org.dawnsci.passerelle.cluster.service.SliceBean;
import org.ggf.drmaa.ExitTimeoutException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author erwindl
 * 
 */
public class DrmaaJobWaiterService {
  private final static Logger LOGGER = LoggerFactory.getLogger(DrmaaJobWaiterService.class);

  private BlockingQueue<JobEntry> busyJobsQueue = new LinkedBlockingQueue<>();

  private ExecutorService jobWaiterExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryForNamedThreads("DrmaaJobWaiterService"));

  private Session session;

  private boolean active;

  public DrmaaJobWaiterService(Session session) {
    this.session = session;
    jobWaiterExecutor.submit(new JobWaiter());
    active = true;
    LOGGER.info("DrmaaJobWaiterService started");
  }

  public void submitjob(AnalysisJobBean job, JobListener listener, long timeout, TimeUnit unit) {
    if (active) {
      busyJobsQueue.add(new JobEntry(job, listener, timeout, unit));
    } else {
      throw new IllegalStateException("DrmaaJobWaiterService not active");
    }
  }

  public void shutDown() {
    LOGGER.trace("shutDown() - entry");
    active = false;
    try {
      jobWaiterExecutor.shutdownNow();
    } catch (Exception e) {
      LOGGER.error("Error shutting down JobWaiter", e);
    }
    // TODO define what to do when the service is deactivated : should we wait for all jobs to finish?
    // or cancel them? or just report a failure to the JobListeners?
    for (JobEntry jobEntry : busyJobsQueue) {
      jobEntry.listener.jobFailed(jobEntry.job, new IllegalStateException("DrmaaJobWaiterService shutdown"));
    }
    LOGGER.info("DrmaaJobWaiterService shutdown");
    LOGGER.trace("shutDown() - exit");
  }

  private static class JobEntry {
    AnalysisJobBean job;
    JobListener listener;
    long timeout;
    TimeUnit unit;

    public JobEntry(AnalysisJobBean job, JobListener listener, long timeout, TimeUnit unit) {
      this.job = job;
      this.listener = listener;
      this.timeout = timeout;
      this.unit = unit;
    }
  }

  private class JobWaiter implements Runnable {
    private static final int JOBQUEUE_BLOCKINGSECONDS = 5;

    @Override
    public void run() {
      try {
        LOGGER.debug("Starting JobWaiter");
        while (active) {
          try {
            JobEntry jobEntry = busyJobsQueue.poll(JOBQUEUE_BLOCKINGSECONDS, TimeUnit.SECONDS);
            if (jobEntry != null) {
              AnalysisJobBean job = jobEntry.job;
              JobListener listener = jobEntry.listener;
              long timeout = jobEntry.unit.toSeconds(jobEntry.timeout);
              try {
                JobInfo jobInfo = session.wait(job.getInternalJobID(), timeout);
                if (jobInfo != null) {
                  job.setExitCode(jobInfo.getExitStatus());
                } else {
                  // might be from a test DRMAA impl that does not support obtaining JobInfo
                  job.setExitCode(0);
                }
                job.setFinished(true);
                job.setOutputSlice(getOutputSlice(job));
                // Remark that if jobFinished would throw an exception
                // we will try to call jobFailed in the catch block below
                listener.jobFinished(job);
              } catch (ExitTimeoutException e) {
                listener.jobTimeOut(job, jobEntry.timeout, jobEntry.unit);
              } catch (Exception e) {
                listener.jobFailed(job, e);
              }
            }
          } catch (InterruptedException e) {
            // ignore
          }
        }
      } catch (Throwable t) {
        LOGGER.error("FATAL error - interrupting JobWaiter thread", t);
      } finally {
        LOGGER.debug("Terminating JobWaiter");
      }
    }

   private SliceBean getOutputSlice(AnalysisJobBean job) {
      File sliceFile = new File(job.getJobFolder(), "resultSlice.properties");
      Properties props = new Properties();
      if (sliceFile.exists()) {
        Reader sliceReader = null;
        try {
          sliceReader = new FileReader(sliceFile);
          props.load(sliceReader);
          return SliceBean.fromProperties(props);
        } catch (Exception e) {
          LOGGER.error("Error loading slice from result file", e);
        } finally {
          if (sliceReader != null) {
            try {
              sliceReader.close();
            } catch (Exception e) {
            }
          }
        }
      } else {
        // TODO should we assume a predefined name for the data with the result data?
        // TODO must we pass the input slice info alongside the result data info?
        return new SliceBean(null, null, null, job.getJobFolder());
      }
      return null;
    }
  }
}
