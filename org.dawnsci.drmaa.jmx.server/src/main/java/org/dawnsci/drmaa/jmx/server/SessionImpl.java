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
package org.dawnsci.drmaa.jmx.server;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.dawnsci.drmaa.jmx.JobInfoBean;
import org.dawnsci.drmaa.jmx.JobTemplateBean;
import org.dawnsci.drmaa.jmx.SessionMXBean;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.InvalidJobTemplateException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.NoActiveSessionException;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author erwindl
 * 
 */
public class SessionImpl implements SessionMXBean {
  private final static Logger LOGGER = LoggerFactory.getLogger(SessionImpl.class);

  static final String SESSION_MXBEAN_NAME = "org.dawnsci.drmaa:type=Session";
  private ObjectName sessionMxbeanName;

  private Map<String, JobTemplateBean> jobTemplates = new ConcurrentHashMap<>();
  private Map<String, JobTemplate> localJobTemplates = new ConcurrentHashMap<>();

  private Session localSession;
  private SessionFactoryImpl factory;

  /**
   * construct a JMX-based remote facade on a local DRMAA Session instance.
   * 
   * @param localSession
   * @param sessionFactoryImpl 
   */
  public SessionImpl(Session localSession, SessionFactoryImpl factory) {
    this.localSession = localSession;
    this.factory = factory;
  }

  void registerMXBean() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
    sessionMxbeanName = new ObjectName(SESSION_MXBEAN_NAME);
    ManagementFactory.getPlatformMBeanServer().registerMBean(this, sessionMxbeanName);
    LOGGER.info("Activated {}", sessionMxbeanName);
  }

  @Override
  public JobTemplateBean createJobTemplate() throws DrmaaException {
    LOGGER.trace("createJobTemplate() - entry");
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      JobTemplate localJt = localSession.createJobTemplate();
      JobTemplateBean jt = new JobTemplateBean(UUID.randomUUID().toString());
      jt.pullDataFrom(localJt);
      jobTemplates.put(jt.getId(), jt);
      localJobTemplates.put(jt.getId(), localJt);
      LOGGER.debug("Created JobTemplateBean {}", jt.getId());
      LOGGER.trace("createJobTemplate() - exit : {}", jt.getId());
      return jt;
    }
  }

  @Override
  public void deleteJobTemplate(JobTemplateBean jt) throws DrmaaException {
    LOGGER.trace("deleteJobTemplate() - entry : {}", jt.getId());
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      JobTemplateBean rm = jobTemplates.remove(jt.getId());
      if (rm == null) {
        throw new InvalidJobTemplateException("JobTemplate " + jt.getId() + " not found in this session");
      } else {
        JobTemplate localJt = localJobTemplates.remove(jt.getId());
        localSession.deleteJobTemplate(localJt);
        LOGGER.debug("Deleted JobTemplate {}", jt.getId());
      }
    }
    LOGGER.trace("deleteJobTemplate() - exit");
  }

  @Override
  public String runJob(JobTemplateBean jt) throws DrmaaException {
    String jobId = jt.getId();
    LOGGER.trace("runJob() - entry : {}", jobId);
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      JobTemplate localJt = localJobTemplates.get(jobId);
      if (localJt != null) {
        jt.pushDataTo(localJt);
        // TODO check what state mgmt is needed in here to track running jobs
        String processId = localSession.runJob(localJt);
        LOGGER.debug("Run Job with process ID {}", processId);
        LOGGER.trace("runJob() - exit : {}", jobId);
        return processId;
      } else {
        throw new InvalidJobTemplateException("Unknown JobTemplate ID " + jobId);
      }
    }
  }

  @Override
  public JobInfoBean wait(String jobId, long timeout) throws DrmaaException {
    LOGGER.trace("wait() - entry : {} - timeout {}", jobId, timeout);
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      JobInfo jobInfo = localSession.wait(jobId, timeout);
      JobInfoBean jb = new JobInfoBean(jobId);
      jb.pullDataFrom(jobInfo);
      LOGGER.debug("Got JobInfo {}", jb);
      LOGGER.trace("wait() - exit : {}", jobId);
      return jb;
    }
  }

  @Override
  public int getJobProgramStatus(String jobId) throws DrmaaException {
    LOGGER.trace("getJobProgramStatus() - entry : {}", jobId);
    int jobStatus = 0;
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      jobStatus = localSession.getJobProgramStatus(jobId);
    }
    LOGGER.trace("getJobProgramStatus() - exit : {} - status {}", jobId, jobStatus);
    return jobStatus;
  }
  
  @Override
  public void init(String contact) throws DrmaaException {
    LOGGER.trace("init() - entry : {}", contact);
    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      localSession.init(contact);
    }
    LOGGER.trace("init() - exit : {}", contact);
  }

  @Override
  public void exit() throws DrmaaException {
    LOGGER.trace("exit() - entry");
    if (sessionMxbeanName != null) {
      try {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(sessionMxbeanName);
        LOGGER.info("Deactivated {}", sessionMxbeanName);
      } catch (Exception e) {
        LOGGER.error("Error unregistering Session MXBean", e);
      }
    }
    factory.clearSession(this);

    if (localSession == null) {
      throw new NoActiveSessionException();
    } else {
      localSession.exit();
      localSession = null;
      jobTemplates.clear();
      localJobTemplates.clear();
    }
    LOGGER.trace("exit() - entry");
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
    LOGGER.trace("setLocalSession - entry {}", localSession);
    if (localSession == null && this.localSession != null) {
      try {
        exit();
      } catch (DrmaaException e) {
        // ignore exceptions here
      }
    } else {
      this.localSession = localSession;
    }
    LOGGER.trace("setLocalSession - exit");
  }
}
