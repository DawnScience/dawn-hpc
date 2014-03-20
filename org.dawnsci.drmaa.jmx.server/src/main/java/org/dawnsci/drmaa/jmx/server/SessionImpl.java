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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.dawnsci.drmaa.jmx.JobTemplateBean;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.InvalidJobTemplateException;

public class SessionImpl implements SessionMXBean {
  
  private Map<String, JobTemplateBean> jobTemplates = new HashMap<String, JobTemplateBean>();

  public JobTemplateBean createJobTemplate() {
    JobTemplateBean jt = new JobTemplateBean(UUID.randomUUID().toString());
    jobTemplates.put(jt.getId(), jt);
    return jt;
  }
  
  public void deleteJobTemplate(JobTemplateBean jt) throws DrmaaException {
    JobTemplateBean rm = jobTemplates.remove(jt.getId());
    if(rm==null) {
      throw new InvalidJobTemplateException("JobTemplate "+jt.getId()+" not found in this session");
    }
  }
}
