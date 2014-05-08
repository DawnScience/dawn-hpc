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
package org.dawnsci.drmaa.jmx;

import java.util.HashMap;
import java.util.Map;

import org.dawnsci.drmaa.common.JobInfoImpl;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;

/**
 * A JMX bean to transport DRMAA JobInfo.
 * <p>
 * Remark that boolean getters are not named as in JobInfo,
 * as the JMX bean needs to follow bean-conventions.
 * </p>
 * @author erwindl
 *
 */
public class JobInfoBean {
  
  private String jobId;
  private boolean exited=true;
  private int exitStatus=0;
  private boolean signaled=false;
  private String terminatingSignal;
  private boolean coreDump=false;
  private boolean aborted=false;
  private Map<String, String> resourceUsage = new HashMap<>();
  
  public JobInfoBean() {
  }
  
  public JobInfoBean(String jobId) {
    this.jobId = jobId;
  }

  public String getJobId() {
    return jobId;
  }
  public void setJobId(String jobId) {
    this.jobId = jobId;
  }
  public boolean isExited() {
    return exited;
  }
  public void setExited(boolean exited) {
    this.exited = exited;
  }
  public int getExitStatus() {
    return exitStatus;
  }
  public void setExitStatus(int exitStatus) {
    this.exitStatus = exitStatus;
  }
  
  public Map<String, String> getResourceUsage() {
    return resourceUsage;
  }
  
  public void setResourceUsage(Map<String, String> resourceUsage) {
    this.resourceUsage = resourceUsage;
  }
  
  public boolean isSignaled() {
    return signaled;
  }
  public void setSignaled(boolean signaled) {
    this.signaled = signaled;
  }
  public String getTerminatingSignal() {
    return terminatingSignal;
  }
  public void setTerminatingSignal(String terminatingSignal) {
    this.terminatingSignal = terminatingSignal;
  }
  public boolean isCoreDump() {
    return coreDump;
  }
  public void setCoreDump(boolean coreDump) {
    this.coreDump = coreDump;
  }
  public boolean isAborted() {
    return aborted;
  }
  public void setAborted(boolean aborted) {
    this.aborted = aborted;
  }
  
  public void pushDataTo(JobInfo localJi) {
    if (localJi != null && localJi instanceof JobInfoImpl) {
      JobInfoImpl lji = (JobInfoImpl) localJi;
      lji.setAborted(isAborted());
      lji.setCoreDump(isCoreDump());
      lji.setExited(isExited());
      lji.setExitStatus(getExitStatus());
      lji.setSignaled(isSignaled());
      lji.setTerminatingSignal(getTerminatingSignal());
      lji.setResourceUsage(getResourceUsage());
    }
  }

  public void pullDataFrom(JobInfo localJi) throws DrmaaException {
    if (localJi != null) {
      setAborted(localJi.wasAborted());
      setCoreDump(localJi.hasCoreDump());
      setExited(localJi.hasExited());
      setExitStatus(localJi.getExitStatus());
      setSignaled(localJi.hasSignaled());
      if(isSignaled())
        setTerminatingSignal(localJi.getTerminatingSignal());
      if(localJi.getResourceUsage()!=null) {
        setResourceUsage(localJi.getResourceUsage());
      }
    }
  }

  @Override
  public String toString() {
    return "JobInfoBean [jobId=" + jobId + ", exited=" + exited + ", exitStatus=" + exitStatus + ", signaled=" + signaled + ", terminatingSignal="
        + terminatingSignal + ", coreDump=" + coreDump + ", aborted=" + aborted + "]";
  }
}
