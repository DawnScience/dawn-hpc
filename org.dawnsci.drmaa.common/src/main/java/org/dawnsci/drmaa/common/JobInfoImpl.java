/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.drmaa.common;

import java.util.HashMap;
import java.util.Map;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;

/**
 * @author erwindl
 * 
 */
public class JobInfoImpl implements JobInfo {

  private String jobId;
  private boolean exited = true;
  private int exitStatus = 0;
  private boolean signaled = false;
  private String terminatingSignal;
  private boolean coreDump = false;
  private boolean aborted = false;
  private Map<String, String> resourceUsage = new HashMap<>();

  
  public JobInfoImpl() {
  }

  /**
   * 
   * @param jobId
   * @param exited
   * @param exitStatus
   * @param aborted
   * @param signaled
   * @param terminatingSignal
   * @param coreDump
   * @param resourceUsage
   */
  public JobInfoImpl(String jobId, boolean exited, int exitStatus, boolean aborted) {
    this.jobId = jobId;
    this.exited = exited;
    this.exitStatus = exitStatus;
    this.aborted = aborted;
  }

  @Override
  public String getJobId() throws DrmaaException {
    return jobId;
  }

  @Override
  public Map<String, String> getResourceUsage() throws DrmaaException {
    return resourceUsage;
  }

  @Override
  public boolean hasExited() throws DrmaaException {
    return exited;
  }

  @Override
  public int getExitStatus() throws DrmaaException {
    return exitStatus;
  }

  @Override
  public boolean hasSignaled() throws DrmaaException {
    return signaled;
  }

  @Override
  public String getTerminatingSignal() throws DrmaaException {
    return terminatingSignal;
  }

  @Override
  public boolean hasCoreDump() throws DrmaaException {
    return coreDump;
  }

  @Override
  public boolean wasAborted() throws DrmaaException {
    return aborted;
  }

  
  public void setExited(boolean exited) {
    this.exited = exited;
  }

  public void setExitStatus(int exitStatus) {
    this.exitStatus = exitStatus;
  }

  public void setSignaled(boolean signaled) {
    this.signaled = signaled;
  }

  public void setTerminatingSignal(String terminatingSignal) {
    this.terminatingSignal = terminatingSignal;
  }

  public void setCoreDump(boolean coreDump) {
    this.coreDump = coreDump;
  }

  public void setAborted(boolean aborted) {
    this.aborted = aborted;
  }

  public void setResourceUsage(Map<String, String> resourceUsage) {
    this.resourceUsage = resourceUsage;
  }

  @Override
  public String toString() {
    return "JobInfoImpl [jobId=" + jobId + ", exited=" + exited + ", exitStatus=" + exitStatus + ", signaled=" + signaled + ", terminatingSignal="
        + terminatingSignal + ", coreDump=" + coreDump + ", aborted=" + aborted + ", resourceUsage=" + resourceUsage + "]";
  }
}
