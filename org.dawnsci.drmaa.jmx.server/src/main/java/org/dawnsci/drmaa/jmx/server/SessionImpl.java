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
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.NoActiveSessionException;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.Version;

public class SessionImpl implements SessionMXBean {

  private Map<String, JobTemplateBean> jobTemplates = new HashMap<>();
  private Map<String, JobTemplate> localJobTemplates = new HashMap<>();

  private Session localSession;

  /**
   * construct a JMX-based remote facade on a local DRMAA Session instance.
   * 
   * @param localSession
   */
  public SessionImpl(Session localSession) {
    this.localSession = localSession;
  }

  @Override
  public JobTemplateBean createJobTemplate() throws DrmaaException {
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      JobTemplate localJt = localSession.createJobTemplate();
      JobTemplateBean jt = new JobTemplateBean(UUID.randomUUID().toString());
      jobTemplates.put(jt.getId(), jt);
      localJobTemplates.put(jt.getId(), localJt);
      return jt;
    }
  }

  @Override
  public void deleteJobTemplate(JobTemplateBean jt) throws DrmaaException {
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      JobTemplateBean rm = jobTemplates.remove(jt.getId());
      if (rm == null) {
        throw new InvalidJobTemplateException("JobTemplate " + jt.getId() + " not found in this session");
      } else {
        JobTemplate localJt = localJobTemplates.remove(jt.getId());
        localSession.deleteJobTemplate(localJt);
      }
    }
  }
  
  @Override
  public String runJob(JobTemplateBean jt) throws DrmaaException {
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      JobTemplate localJt = localJobTemplates.get(jt.getId());
      jt.pushData(localJt);
      // TODO check what state mgmt is needed in here to track running jobs
      return localSession.runJob(localJt);
    }
  }
  
  @Override
  public void init(String contact) throws DrmaaException {
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      localSession.init(contact);
    }
  }

  @Override
  public void exit() throws DrmaaException {
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      localSession.exit();
      localSession = null;
      jobTemplates.clear();
      localJobTemplates.clear();
    }
  }

  @Override
  public String getContact() throws DrmaaException {
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      return localSession.getContact();
    }
  }
  @Override
  public String getDrmaaImplementation() throws DrmaaException {
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      return localSession.getDrmaaImplementation();
    }
  }
  @Override
  public String getDrmSystem() throws DrmaaException {
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      return localSession.getDrmSystem();
    }
  }
  @Override
  public Version getVersion() throws DrmaaException {
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      return localSession.getVersion();
    }
  }
  
  
  Session getLocalSession() {
    return localSession;
  }

  void setLocalSession(Session localSession) {
    if (localSession == null && this.localSession != null) {
      try {
        exit();
      } catch (DrmaaException e) {
        // ignore exceptions here
      }
    } else {
      this.localSession = localSession;
    }
  }
}
