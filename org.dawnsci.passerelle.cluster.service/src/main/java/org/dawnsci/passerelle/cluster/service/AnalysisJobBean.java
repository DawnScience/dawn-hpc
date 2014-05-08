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

package org.dawnsci.passerelle.cluster.service;

import java.io.File;

/**
 * @author erwindl
 * 
 */
public class AnalysisJobBean implements IJob {

  private String initiator;
  private String correlationID;
  private String internalJobID;
  private SliceBean inputSlice;

  private File jobFolder;

  private boolean finished;
  private int exitCode;
  private String exitMessage;

  private SliceBean outputSlice;

  public AnalysisJobBean(String initiator, File jobFolder, String correlationID, SliceBean slice) {
    this.initiator = initiator;
    this.jobFolder = jobFolder;
    this.correlationID = correlationID;
    this.inputSlice = slice;
  }

  public String getCorrelationID() {
    return correlationID;
  }

  public String getInternalJobID() {
    return internalJobID;
  }

  public File getJobFolder() {
    return jobFolder;
  }

  public String getInitiator() {
    return initiator;
  }

  public void setInitiator(String initiator) {
    this.initiator = initiator;
  }

  public void setInternalJobID(String internalJobID) {
    this.internalJobID = internalJobID;
  }

  public SliceBean getInputSlice() {
    return inputSlice;
  }

  public SliceBean getOutputSlice() {
    return outputSlice;
  }

  public void setOutputSlice(SliceBean outputSlice) {
    this.outputSlice = outputSlice;
  }

  public boolean isFinished() {
    return finished;
  }

  public void setFinished(boolean finished) {
    this.finished = finished;
  }

  public int getExitCode() {
    return exitCode;
  }

  public void setExitCode(int exitCode) {
    this.exitCode = exitCode;
  }

  public String getExitMessage() {
    return exitMessage;
  }

  public void setExitMessage(String exitMessage) {
    this.exitMessage = exitMessage;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + exitCode;
    result = prime * result + ((exitMessage == null) ? 0 : exitMessage.hashCode());
    result = prime * result + (finished ? 1231 : 1237);
    result = prime * result + ((internalJobID == null) ? 0 : internalJobID.hashCode());
    result = prime * result + ((inputSlice == null) ? 0 : inputSlice.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AnalysisJobBean other = (AnalysisJobBean) obj;
    if (exitCode != other.exitCode)
      return false;
    if (exitMessage == null) {
      if (other.exitMessage != null)
        return false;
    } else if (!exitMessage.equals(other.exitMessage))
      return false;
    if (finished != other.finished)
      return false;
    if (internalJobID == null) {
      if (other.internalJobID != null)
        return false;
    } else if (!internalJobID.equals(other.internalJobID))
      return false;
    if (inputSlice == null) {
      if (other.inputSlice != null)
        return false;
    } else if (!inputSlice.equals(other.inputSlice))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AnalysisJobBean [jobFolder=" + jobFolder + ", internalJobID=" + internalJobID + ", slice=" + inputSlice + ", finished=" + finished + ", exitCode="
        + exitCode + ", exitMessage=" + exitMessage + "]";
  }
}
