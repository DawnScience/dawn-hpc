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

package org.dawnsci.drmaa.jmx.client;

import java.util.List;
import java.util.UUID;

import org.dawnsci.drmaa.common.JobInfoImpl;
import org.dawnsci.drmaa.common.JobTemplateImpl;
import org.dawnsci.drmaa.jmx.JobInfoBean;
import org.dawnsci.drmaa.jmx.JobTemplateBean;
import org.dawnsci.drmaa.jmx.SessionMXBean;
import org.ggf.drmaa.DrmCommunicationException;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.Version;

/**
 * @author erwindl
 *
 */
public class SessionImpl implements Session {
  
  private SessionMXBean remoteSession;
  private SessionFactoryImpl factory;
  
  public SessionImpl(SessionMXBean remoteSession, SessionFactoryImpl factory) {
    this.remoteSession = remoteSession;
    this.factory = factory;
  }

  @Override
  public void init(String contact) throws DrmaaException {
    remoteSession.init(contact);
  }

  @Override
  public void exit() throws DrmaaException {
    remoteSession.exit();
    factory.clearSession(this);
  }

  @Override
  public JobTemplate createJobTemplate() throws DrmaaException {
    JobTemplateBean jobTemplateBean = remoteSession.createJobTemplate();
    JobTemplate jt = new JobTemplateImpl(UUID.fromString(jobTemplateBean.getId()));
    jobTemplateBean.pushDataTo(jt);
    return jt;
  }

  @Override
  public void deleteJobTemplate(JobTemplate jt) throws DrmaaException {
    JobTemplateBean jtb = new JobTemplateBean(((JobTemplateImpl)jt).getId().toString());
    jtb.pullDataFrom(jt);
    remoteSession.deleteJobTemplate(jtb);
  }

  @Override
  public String runJob(JobTemplate jt) throws DrmaaException {
    JobTemplateBean jtb = new JobTemplateBean(((JobTemplateImpl)jt).getId().toString());
    jtb.pullDataFrom(jt);
    return remoteSession.runJob(jtb);
  }

  @Override
  public List<String> runBulkJobs(JobTemplate jt, int start, int end, int incr) throws DrmaaException {
    throw new DrmCommunicationException("runBulkJobs not supported via JMX gateway");
  }

  @Override
  public void control(String jobId, int action) throws DrmaaException {
    throw new DrmCommunicationException("control not supported via JMX gateway");
  }

  @Override
  public void synchronize(List<String> jobIds, long timeout, boolean dispose) throws DrmaaException {
    throw new DrmCommunicationException("synchronize not supported via JMX gateway");
  }

  @Override
  public JobInfo wait(String jobId, long timeout) throws DrmaaException {
    JobInfoBean jib = remoteSession.wait(jobId, timeout);
    JobInfoImpl ji = new JobInfoImpl();
    jib.pushDataTo(ji);
    return ji;
  }

  @Override
  public int getJobProgramStatus(String jobId) throws DrmaaException {
    return remoteSession.getJobProgramStatus(jobId);
  }

  @Override
  public String getContact() throws DrmaaException {
    return remoteSession.getContact();
  }

  @Override
  public Version getVersion() throws DrmaaException {
    return remoteSession.getVersion();
  }

  @Override
  public String getDrmSystem() throws DrmaaException {
    return remoteSession.getDrmSystem();
  }

  @Override
  public String getDrmaaImplementation() throws DrmaaException {
    return remoteSession.getDrmaaImplementation();
  }

}
