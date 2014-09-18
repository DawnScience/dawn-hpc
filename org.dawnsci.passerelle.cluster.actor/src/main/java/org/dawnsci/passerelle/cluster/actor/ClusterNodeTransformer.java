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

package org.dawnsci.passerelle.cluster.actor;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.dawb.passerelle.common.DatasetConstants;
import org.dawb.passerelle.common.message.MessageUtils;
import org.dawnsci.passerelle.cluster.actor.internal.WorkflowServiceHolder;
import org.dawnsci.passerelle.cluster.service.AnalysisJobBean;
import org.dawnsci.passerelle.cluster.service.IJob;
import org.dawnsci.passerelle.cluster.service.JobListener;
import org.dawnsci.passerelle.cluster.service.ScalarNames;
import org.dawnsci.passerelle.cluster.service.SliceBean;
import org.eclipse.dawnsci.analysis.api.message.DataMessageComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.util.ptolemy.StringChoiceParameter;

/**
 * @author erwindl
 * 
 */
public class ClusterNodeTransformer extends Actor {
  private static final long serialVersionUID = -5458392962680386276L;

  protected static final List<String> TIMEUNIT_CHOICES;
  static {
    TIMEUNIT_CHOICES = new ArrayList<String>(3);
    TIMEUNIT_CHOICES.add(TimeUnit.MINUTES.name());
    TIMEUNIT_CHOICES.add(TimeUnit.SECONDS.name());
    TIMEUNIT_CHOICES.add(TimeUnit.MILLISECONDS.name());
  }

  private final static Logger LOGGER = LoggerFactory.getLogger(ClusterNodeTransformer.class);

  public Port input;
  public Port output;

  /**
   * defines the runtime command to execute the analysis workflow
   */
  public StringParameter runtimeParameter;

  /**
   * defines the model file containing the analysis workflow that must be executed
   */
  public FileParameter workflowFileParameter;

  public StringParameter extraJobArgsParameter;

  public Parameter timeoutParameter;
  public StringChoiceParameter timeUnitParameter;

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

    runtimeParameter = new StringParameter(this, "Runtime");
    runtimeParameter.setExpression("runWorkflow.sh");
    registerExpertParameter(runtimeParameter);

    workflowFileParameter = new FileParameter(this, "Workflow");

    extraJobArgsParameter = new StringParameter(this, "Extra job args");
    new TextStyle(extraJobArgsParameter, "paramsTextArea");

    timeoutParameter = new Parameter(this, "timeout", new IntToken(10));
    timeUnitParameter = new StringChoiceParameter(this, "time unit", TIMEUNIT_CHOICES, 1 << 2 /* SWT.SINGLE */);
    timeUnitParameter.setExpression(TimeUnit.SECONDS.name());
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected ProcessingMode getProcessingMode(ActorContext ctxt, ProcessRequest request) {
    return ProcessingMode.ASYNCHRONOUS;
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage message = request.getMessage(input);
    DataMessageComponent dmc = null;
    try {
      dmc = MessageUtils.coerceMessage(message);
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error getting DataMessageComponent from received message", this, message, e);
    }
    SliceBean slice = new SliceBean(dmc.getScalar(ScalarNames.DATASET), dmc.getScalar(ScalarNames.SLICE), dmc.getScalar(ScalarNames.SHAPE), new File(
        dmc.getScalar(ScalarNames.FILEPATH)));

    try {
      long timeout = ((IntToken) timeoutParameter.getToken()).longValue();
      TimeUnit timeUnit = TimeUnit.valueOf(timeUnitParameter.stringValue());

      AnalysisJobBean job = WorkflowServiceHolder.getInstance().getClusterService().submitAnalysisJob(
              System.getProperty("user.name", "DAWN"), Long.toString(message.getID()), 
              runtimeParameter.stringValue(),
              workflowFileParameter.asFile().getAbsolutePath(), slice, 
              getExtraJobArgs(),
              timeout, timeUnit, 
              new AnalysisJobListener(message.getID(), response));

      wipQueue.add(job.getCorrelationID());
    } catch (IllegalActionException e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error reading actor parameters", this, message, e);
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Job submission failed", this, message, e);
    }
  }

  @Override
  protected boolean doPostFire() throws ProcessingException {
    boolean result = super.doPostFire();
    if (!result) {
      // Before actually declaring that the actor has no more work, by returning false,
      // we need to make sure all jobs are finished and have sent out their results.
      // Remark that this wait-loop works fine for thread-based model execution,
      // but for event-based ones it would be better to devise another approach so that
      // its single execution thread doesn't hang here.
      synchronized (wipQueue) {
        while (!wipQueue.isEmpty()) {
          try {
            wipQueue.wait(1000);
          } catch (InterruptedException e) {
            break;
          }
        }
      }
    }
    return result;
  }

  private Map<String, String> getExtraJobArgs() {
    try {
      String paramDefs = ((StringToken) extraJobArgsParameter.getToken()).stringValue();
      if (paramDefs != null && !paramDefs.trim().isEmpty()) {
        Map<String, String> extraArgsMap = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader(new StringReader(paramDefs));
        String paramDef = null;
        while ((paramDef = reader.readLine()) != null) {
          String[] paramKeyValue = paramDef.split("=");
          extraArgsMap.put(paramKeyValue[0], paramKeyValue[1]);
        }
        return extraArgsMap;
      }
    } catch (Exception e) {
      getLogger().error(ErrorCode.ACTOR_EXECUTION_ERROR + " - Error reading extra job args", e);
    }
    return null;
  }

  /**
   * This Deque maintains the ordered list of submitted jobs, i.e the Work In Progress. A Deque is used to ensure that
   * result messages are sent out in the right order. Concurrent implementations are needed as listener callbacks may be
   * coming in from multiple threads.
   */
  private Queue<String> wipQueue = new ConcurrentLinkedQueue<>();
  private Map<String, ProcessResponse> finishedJobs = new ConcurrentHashMap<>();

  private void checkWIP() {
    synchronized (wipQueue) {
      if (!wipQueue.isEmpty()) {
        String jobInLine = wipQueue.peek();
        if (finishedJobs.containsKey(jobInLine)) {
          wipQueue.poll();
          ProcessResponse response = finishedJobs.remove(jobInLine);
          processFinished(response.getContext(), response.getRequest(), response);
          wipQueue.notifyAll();
          // and try to pop next one as well, its termination notification may have arrived before as well
          checkWIP();
        }
      }
    }
  }

  private class AnalysisJobListener implements JobListener {
    private Long causeMsgID;
    private ProcessResponse processResponse;

    public AnalysisJobListener(Long causeMsgID, ProcessResponse processResponse) {
      this.causeMsgID = causeMsgID;
      this.processResponse = processResponse;
    }

    @Override
    public void jobFinished(IJob job) {
      AnalysisJobBean ajb = (AnalysisJobBean) job;
      if (ajb.getOutputSlice() != null) {
        SliceBean sliceBean = ajb.getOutputSlice();

        ManagedMessage resultMsg = createMessage();
        resultMsg.addCauseID(causeMsgID);
        final DataMessageComponent comp = new DataMessageComponent();
        // TODO must we pass the input slice info alongside this result data info??
        if (sliceBean.getDataSet() != null)
          comp.putScalar(ScalarNames.DATASET, sliceBean.getDataSet());
        if (sliceBean.getSlice() != null)
          comp.putScalar(ScalarNames.SLICE, sliceBean.getSlice());
        if (sliceBean.getShape() != null)
          comp.putScalar(ScalarNames.SHAPE, sliceBean.getShape());
        if (sliceBean.getFile() != null) {
          comp.putScalar(ScalarNames.FILENAME, sliceBean.getFile().getName());
          comp.putScalar(ScalarNames.FILEPATH, sliceBean.getFile().getAbsolutePath());
        }

        try {
          resultMsg.setBodyContent(comp, DatasetConstants.CONTENT_TYPE_DATA);
          processResponse.addOutputMessage(output, resultMsg);
        } catch (MessageException e) {
          processResponse.setException(new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Failed to create result msg for " + job,
              ClusterNodeTransformer.this, e));
        }
      } else {
        processResponse.setException(new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Job execution finished without results for " + job,
            ClusterNodeTransformer.this, null));
      }
      finishedJobs.put(job.getCorrelationID(), processResponse);
      checkWIP();
    }

    @Override
    public void jobFailed(IJob job, Throwable t) {
      processResponse.setException(new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Job execution failed for " + job, ClusterNodeTransformer.this, t));
      finishedJobs.put(job.getCorrelationID(), processResponse);
      checkWIP();
    }

    @Override
    public void jobTimeOut(IJob job, long timeout, TimeUnit unit) {
      processResponse.setException(new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Job time out for " + job, ClusterNodeTransformer.this, null));
      finishedJobs.put(job.getCorrelationID(), processResponse);
      checkWIP();
    }
  }
}
