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
package org.dawnsci.drmaa.howto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

public class HowToSubmitJavaApp {
  static void usage() {
    System.out.println("org.dawnsci.drmaa.howto.HowToSubmitJavaApp [sleep s] [exit code]");
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      usage();
      System.exit(1);
    }
    
    SessionFactory factory = SessionFactory.getFactory();
    Session session = factory.getSession();

    try {
      session.init("");
      JobTemplate jt = session.createJobTemplate();
      jt.setRemoteCommand("java");
      List<String> jobArgs = new ArrayList<String>();
      jobArgs.add("-cp");
      jobArgs.add("/dls_sw/apps/DawnDiamond/cluster/classes");
      jobArgs.add("trials.TestExitStatus");
      jobArgs.add(args[0]);
      jobArgs.add(args[1]);
      jt.setArgs(jobArgs);

      String id = session.runJob(jt);

      System.out.println("Your job has been submitted with id " + id);

      session.deleteJobTemplate(jt);
      
      JobInfo info = session.wait(id, Session.TIMEOUT_WAIT_FOREVER);
      
      if (info.wasAborted()) {
         System.out.println("Job " + info.getJobId() + " never ran");
      } else if (info.hasExited()) {
         System.out.println("Job " + info.getJobId() +
               " finished regularly with exit status " +
               info.getExitStatus());
      } else if (info.hasSignaled()) {
         System.out.println("Job " + info.getJobId() +
               " finished due to signal " +
               info.getTerminatingSignal());
      } else {
         System.out.println("Job " + info.getJobId() +
               " finished with unclear conditions");
      }
      
      System.out.println("Job Usage:");
      
      Map<String, String> rmap = info.getResourceUsage();
      Iterator<String> i = rmap.keySet().iterator();
      
      while (i.hasNext()) {
         String name = (String)i.next();
         String value = (String)rmap.get(name);
         
         System.out.println("  " + name + "=" + value);
      }
      
      session.exit();
    } catch (DrmaaException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }
}
