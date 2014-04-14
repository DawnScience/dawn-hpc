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

package org.dawnsci.passerelle.cluster.actor;

import java.io.File;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.dawb.passerelle.common.message.MessageUtils;
import org.dawnsci.passerelle.cluster.actor.internal.AnalysisJobBean;
import org.dawnsci.passerelle.cluster.actor.internal.DrmaaSessionFactoryHolder;
import org.dawnsci.passerelle.cluster.actor.internal.SliceBean;
import org.ggf.drmaa.AlreadyActiveSessionException;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import uk.ac.diamond.scisoft.analysis.message.DataMessageComponent;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ptolemy.DirectoryParameter;

/**
 * @author erwindl
 * 
 */
public class ClusterNodeTransformer extends Actor {
  private static final long serialVersionUID = -5458392962680386276L;

  private final static Logger LOGGER = LoggerFactory.getLogger(ClusterNodeTransformer.class);

  public Port input;
  public Port output;

  /**
   * defines the folder where the output files will be written by the cluster job
   */
  public FileParameter outputFolder;

  /**
   * defines the model file containing the analysis workflow that must be executed
   */
  public FileParameter workflowFile;

  /**
   * The DRMAA session
   */
  private Session session;
  
  private Deque<AnalysisJobBean> submittedJobs = new LinkedList<>(); 

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public ClusterNodeTransformer(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, DataMessageComponent.class);
    output = PortFactory.getInstance().createOutputPort(this);

    outputFolder = new DirectoryParameter(this, "Output folder");
    workflowFile = new FileParameter(this, "Workflow");
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    try {
      submittedJobs.clear();
      // TODO review is this is the right place to initialize a DRMAA session
      // It may be better to do this in a more central spot that can also manage the shutdown in a centralized manner.
      session = DrmaaSessionFactoryHolder.getInstance().getSessionFactory().getSession();
      session.init(null);
    } catch (AlreadyActiveSessionException e) {
      // ignore as this could just imply that DRMAA is already ready for us
    } catch (Exception e) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Failed to initialize a DRMAA Session", this, e);
    }
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(input);
    DataMessageComponent dmc = null;
    try {
      dmc = MessageUtils.coerceMessage(msg);
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error getting DataMessageComponent from received message", this, msg, e);
    }
    SliceBean slice = new SliceBean(dmc.getScalar("dataSet"), dmc.getScalar("slice"), dmc.getScalar("shape"), new File(dmc.getScalar("file_path")));
    
    try {
      AnalysisJobBean jobBean = new AnalysisJobBean(1L, slice);
      JobTemplate jobTemplate = session.createJobTemplate();
      jobTemplate.setRemoteCommand("java");
      List<String> args = new ArrayList<>();
      args.add("-jar");
      args.add("C:/temp/dls_trials/bin/PasserelleRuntime.jar");
      args.add(workflowFile.asFile().getAbsolutePath());
      args.add("jobID="+jobBean.getControlJobID());
      args.add("dataFile="+slice.getFilePath());
      args.add("dataSet="+slice.getDataSet());
      jobTemplate.setArgs(args);
      String jobId = session.runJob(jobTemplate);
      jobBean.setGridJobID(jobId);
      submittedJobs.addLast(jobBean);
      
      getLogger().info("Submitted job {}", jobId);
    } catch (DrmaaException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalActionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
