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
package org.dawnsci.drmaa.jmx.server;

import org.dawnsci.drmaa.jmx.JobTemplateBean;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.Version;

public interface SessionMXBean {

  JobTemplateBean createJobTemplate() throws DrmaaException;
  void deleteJobTemplate(JobTemplateBean jt) throws DrmaaException;
  
  String runJob(JobTemplateBean jt) throws DrmaaException;
  
  void init(String contact) throws DrmaaException;
  void exit() throws DrmaaException;
  
  
  String getContact() throws DrmaaException;
  Version getVersion() throws DrmaaException;
  String getDrmSystem() throws DrmaaException;
  String getDrmaaImplementation() throws DrmaaException;
}
