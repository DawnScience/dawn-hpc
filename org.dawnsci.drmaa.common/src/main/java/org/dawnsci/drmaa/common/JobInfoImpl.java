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
