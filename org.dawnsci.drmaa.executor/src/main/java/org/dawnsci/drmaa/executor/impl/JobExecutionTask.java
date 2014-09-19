/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.drmaa.executor.impl;

import java.util.concurrent.RunnableFuture;

import org.dawnsci.drmaa.common.JobTemplateImpl;
import org.dawnsci.drmaa.executor.impl.cmdline.ManagedCommandline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author erwindl
 *
 */
public class JobExecutionTask implements CancellableTask<Integer> {
  private final static Logger LOGGER = LoggerFactory.getLogger(JobExecutionTask.class);
  private final static String LINE_SEPARATOR = System.getProperty("line.separator");

  private JobTemplateImpl jobTemplate;
  private Integer bulkJobIndex;
  private Process jobProcess;
  private String id;

  public JobExecutionTask(JobTemplateImpl jt) {
    this.jobTemplate = jt;
  }
  
  public JobExecutionTask(JobTemplateImpl jt, int bulkJobIndex) {
    this.jobTemplate = jt;
    this.bulkJobIndex = bulkJobIndex;
  }
  
  public String getId() {
    if(id==null) {
      StringBuilder strBldr = new StringBuilder(jobTemplate.getId().toString());
      if(isBulkJob()) {
        strBldr.append("[").append(getBulkJobIndex()).append("]");
      }
      id = strBldr.toString();
    }
    return id;
  }
  
  public boolean isBulkJob() {
    return bulkJobIndex!=null;
  }
  
  public Integer getBulkJobIndex() {
    return bulkJobIndex;
  }
  
  @Override
  public RunnableFuture<Integer> newFutureTask() {
    return new JobExecutionFuture(this);
  }

  @Override
  public Integer call() throws Exception {
    ManagedCommandline cmdLine = new ManagedCommandline(jobTemplate.getRemoteCommand());
    cmdLine.setWorkingDirectory(jobTemplate.getWorkingDirectory());
    for(String arg : jobTemplate.getArgs()) {
      cmdLine.createArgument().setValue(arg);
    }
    Integer exitStatus = null;
    try {
      LOGGER.debug("Starting Job {} using {}", getId(), cmdLine.getExecutable());
      jobProcess = cmdLine.execute();
      LOGGER.info("Started Job {} using {}", getId(), cmdLine.getExecutable());
      jobProcess = cmdLine.waitForProcessFinished();
      exitStatus = jobProcess.waitFor();
      LOGGER.info("Finished Job {} - exit code {}", this, exitStatus);
      LOGGER.debug("Process output" + LINE_SEPARATOR + "\t" + cmdLine.getStdoutAsString());
      LOGGER.debug("Process error" + LINE_SEPARATOR + "\t" + cmdLine.getStderrAsString());
    } catch (Exception e) {
      LOGGER.error("Error starting Job "+jobTemplate.getJobName(), e);
      exitStatus = 2;
    }
    return exitStatus;
  }
  
  @Override
  public void cancel() {
    LOGGER.debug("Cancelling Job {}", this);
    jobProcess.destroy();
    LOGGER.info("Cancelled Job {}", this);
  }

  @Override
  public String toString() {
    StringBuilder strBldr = new StringBuilder(getId());
    strBldr.append(" (").append(jobTemplate.getRemoteCommand()).append(")");
    return strBldr.toString();
  }
}
