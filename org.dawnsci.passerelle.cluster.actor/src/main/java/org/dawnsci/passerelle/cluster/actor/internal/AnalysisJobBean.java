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

package org.dawnsci.passerelle.cluster.actor.internal;


/**
 * @author erwindl
 *
 */
public class AnalysisJobBean {
  
  private Long controlJobID;
  private String gridJobID;
  private SliceBean slice;

  public AnalysisJobBean(Long controlJobID, SliceBean slice) {
    this(controlJobID, null, slice);
  }

  public AnalysisJobBean(Long controlJobID, String gridJobID, SliceBean slice) {
    this.controlJobID = controlJobID;
    this.gridJobID = gridJobID;
    this.slice = slice;
  }

  public Long getControlJobID() {
    return controlJobID;
  }

  public String getGridJobID() {
    return gridJobID;
  }
  
  public void setGridJobID(String gridJobID) {
    this.gridJobID = gridJobID;
  }

  public SliceBean getSlice() {
    return slice;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((controlJobID == null) ? 0 : controlJobID.hashCode());
    result = prime * result + ((gridJobID == null) ? 0 : gridJobID.hashCode());
    result = prime * result + ((slice == null) ? 0 : slice.hashCode());
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
    if (controlJobID == null) {
      if (other.controlJobID != null)
        return false;
    } else if (!controlJobID.equals(other.controlJobID))
      return false;
    if (gridJobID == null) {
      if (other.gridJobID != null)
        return false;
    } else if (!gridJobID.equals(other.gridJobID))
      return false;
    if (slice == null) {
      if (other.slice != null)
        return false;
    } else if (!slice.equals(other.slice))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AnalysisJobBean [controlJobID=" + controlJobID + ", gridJobID=" + gridJobID + ", slice=" + slice + "]";
  }
}
