/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.drmaa.jmx;

import org.dawnsci.drmaa.jmx.JobInfoBean;
import org.dawnsci.drmaa.jmx.JobTemplateBean;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.Version;

public interface SessionMXBean {

  JobTemplateBean createJobTemplate() throws DrmaaException;
  void deleteJobTemplate(JobTemplateBean jt) throws DrmaaException;
  
  String runJob(JobTemplateBean jt) throws DrmaaException;
  JobInfoBean wait(String jobId, long timeout) throws DrmaaException;
  
  void init(String contact) throws DrmaaException;
  void exit() throws DrmaaException;
  
  
  String getContact() throws DrmaaException;
  Version getVersion() throws DrmaaException;
  String getDrmSystem() throws DrmaaException;
  String getDrmaaImplementation() throws DrmaaException;
  int getJobProgramStatus(String jobId) throws DrmaaException;
}
