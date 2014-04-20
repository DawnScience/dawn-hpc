package org.dawnsci.drmaa.executor.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.drmaa.common.JobTemplateImpl;
import org.dawnsci.drmaa.executor.SessionImpl;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.NoActiveSessionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestSessionImpl {
  private SessionImpl session;

  @Before
  public void setUp() throws Exception {
    session = new SessionImpl(3);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testInit() throws DrmaaException {
    session.init(null);
    assertEquals("localhost", session.getContact());
  }

  @Test
  public void testExit() {
    fail("Not yet implemented");
  }

  @Test
  public void testCreateJobTemplateNoInit() throws DrmaaException {
    try {
      session.createJobTemplate();
      fail("createJobTemplate should fail for uninitialized session");
    } catch (NoActiveSessionException e) {
      // this is as it should be
    }
  }

  @Test
  public void testDeleteJobTemplateNoInit() throws DrmaaException {
    try {
      session.deleteJobTemplate(null);
      fail("deleteJobTemplate should fail for uninitialized session");
    } catch (NoActiveSessionException e) {
      // this is as it should be
    }
  }

  @Test
  public void testCreateJobTemplate() throws DrmaaException {
    session.init(null);
    JobTemplate jobTemplate = session.createJobTemplate();
    assertTrue("Session must create an instance of our JobTemplateImpl", jobTemplate instanceof JobTemplateImpl);
  }

  @Test
  public void testDeleteJobTemplate() {
    fail("Not yet implemented");
  }

  @Test
  public void testRunJob() throws DrmaaException, InterruptedException {
    session.init(null);
    JobTemplate jobTemplate = session.createJobTemplate();
    jobTemplate.setRemoteCommand("notepad");
    String jobId = session.runJob(jobTemplate);
    session.synchronize(Arrays.asList(jobId), 10, true);
    Thread.sleep(1000);
  }

  @Test
  public void testRunModel() throws DrmaaException, InterruptedException {
    session.init(null);
    JobTemplate jobTemplate = session.createJobTemplate();
    jobTemplate.setRemoteCommand("java");
    List<String> args = new ArrayList<>();
    args.add("-jar");
    args.add("C:/temp/dls_trials/bin/PasserelleRuntime.jar");
    args.add("C:/temp/dls_trials/models/AnalysisMockFlow.moml");
    args.add("jobID=2");
    jobTemplate.setArgs(args);
    String jobId = session.runJob(jobTemplate);
    session.synchronize(Arrays.asList(jobId), 10, true);
    Thread.sleep(1000);
  }

  @Test
  public void testGetContact() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetVersion() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetDrmSystem() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetDrmaaImplementation() {
    fail("Not yet implemented");
  }

}
