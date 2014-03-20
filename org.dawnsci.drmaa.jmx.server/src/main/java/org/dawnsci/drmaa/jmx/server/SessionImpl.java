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
