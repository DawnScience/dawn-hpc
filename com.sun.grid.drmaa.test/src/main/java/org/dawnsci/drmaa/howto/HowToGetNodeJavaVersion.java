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
import java.util.List;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

public class HowToGetNodeJavaVersion {

  public static void main(String[] args) {
    
    SessionFactory factory = SessionFactory.getFactory();
    Session session = factory.getSession();

    try {
      session.init("");
      JobTemplate jt = session.createJobTemplate();
      jt.setRemoteCommand("java");
      List<String> jobArgs = new ArrayList<String>();
      jobArgs.add("-version");
      jt.setArgs(jobArgs);

      String id = session.runJob(jt);

      System.out.println("Your job has been submitted with id " + id);

      session.deleteJobTemplate(jt);
      
      session.exit();
    } catch (DrmaaException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }
}
