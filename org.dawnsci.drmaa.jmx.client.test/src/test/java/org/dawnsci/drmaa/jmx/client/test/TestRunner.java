package org.dawnsci.drmaa.jmx.client.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

public class TestRunner implements CommandProvider {

  public String getHelp() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("\n---JMX Client test---\n");
    buffer.append("\tsubmitSimpleSleepJob <time> <repeat> <jobcategory> <native spec>\n");
    buffer.append("\tlaunchWorkflow <runtime> <workflowFile> <workingDir> <jobcategory> <native spec>\n");
    return buffer.toString();
  }

  public void _submitSimpleSleepJob(CommandInterpreter ci) {
    String time = ci.nextArgument();
    String repeat = ci.nextArgument();
    int repeatCnt = Integer.parseInt(repeat);
    String jobCategory = ci.nextArgument();
    String nativeSpec = ci.nextArgument();

    SessionFactory factory = SessionFactory.getFactory();
    Session session = factory.getSession();

    try {
      try {
        session.init(null);
      } catch (DrmaaException e) {

      }
      JobTemplate jt = session.createJobTemplate();
      jt.setRemoteCommand("sleep");
      jt.setArgs(Collections.singletonList(time != null ? time : "5"));
      if (jobCategory != null) {
        System.out.println("setting " + jobCategory);
        jt.setJobCategory(jobCategory);
      }
      if (nativeSpec != null) {
        System.out.println("setting native spec :" + nativeSpec);
        jt.setNativeSpecification(nativeSpec);
      }

      List<String> jobIDs = new ArrayList<>();
      for (int i = 0; i < repeatCnt; ++i) {
        String jobID = session.runJob(jt);
        jobIDs.add(jobID);
        System.out.println("Your job has been submitted with id " + jobID);
      }

      session.deleteJobTemplate(jt);

      session.synchronize(jobIDs, 60, false);
      for (int i = 0; i < repeatCnt; ++i) {
        waitAndReport(session, jobIDs.get(i));
      }
      session.exit();
    } catch (DrmaaException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  public void _launchWorkflow(CommandInterpreter ci) {
    String runtimeCmd = ci.nextArgument();
    String workflowFile = ci.nextArgument();
    String workingDir = ci.nextArgument();
    String jobCategory = ci.nextArgument();
    String nativeSpec = ci.nextArgument();

    if (runtimeCmd == null || workflowFile == null) {
      System.err.println(getHelp());
    } else {
      SessionFactory factory = SessionFactory.getFactory();
      Session session = factory.getSession();
      try {
        try {
          session.init(null);
        } catch (DrmaaException e) {
        }
        JobTemplate jt = session.createJobTemplate();
        jt.setRemoteCommand(runtimeCmd);
        List<String> args = new ArrayList<>(3);
        args.add(workflowFile);
        args.add("workingDir=" + workingDir);
        jt.setArgs(args);

        if (jobCategory != null) {
          System.out.println("setting " + jobCategory);
          jt.setJobCategory(jobCategory);
        }
        if (nativeSpec != null) {
          System.out.println("setting native spec :" + nativeSpec);
          jt.setNativeSpecification(nativeSpec);
        }

        String id = session.runJob(jt);

        System.out.println("Your job has been submitted with id " + id);

        session.deleteJobTemplate(jt);

        waitAndReport(session, id);

        session.exit();
      } catch (DrmaaException e) {
        System.out.println("Error: " + e.getMessage());
      }
    }
  }

  void waitAndReport(Session session, String id) throws DrmaaException {
    JobInfo info = null;
    try {
      info = session.wait(id, Session.TIMEOUT_WAIT_FOREVER);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (info.wasAborted()) {
      System.out.println("Job " + info.getJobId() + " never ran");
    } else if (info.hasExited()) {
      System.out.println("Job " + info.getJobId() + " finished regularly with exit status " + info.getExitStatus());
    } else if (info.hasSignaled()) {
      System.out.println("Job " + info.getJobId() + " finished due to signal " + info.getTerminatingSignal());
    } else {
      System.out.println("Job " + info.getJobId() + " finished with unclear conditions");
    }

    System.out.println("Job Usage:");

    Map<String, String> rmap = info.getResourceUsage();
    Iterator<String> i = rmap.keySet().iterator();

    while (i.hasNext()) {
      String name = (String) i.next();
      String value = (String) rmap.get(name);

      System.out.println("  " + name + "=" + value);
    }
  }

}
