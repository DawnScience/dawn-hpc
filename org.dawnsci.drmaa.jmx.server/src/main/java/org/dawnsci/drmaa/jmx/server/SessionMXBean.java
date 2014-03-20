package org.dawnsci.drmaa.jmx.server;

import org.dawnsci.drmaa.jmx.JobTemplateBean;
import org.ggf.drmaa.DrmaaException;

public interface SessionMXBean {

  JobTemplateBean createJobTemplate();
  void deleteJobTemplate(JobTemplateBean jt) throws DrmaaException;
  
}
