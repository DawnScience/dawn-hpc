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
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.dawb.passerelle.common.message.MessageUtils;
import org.dawb.passerelle.common.parameter.ParameterUtils;
import org.dawb.passerelle.common.utils.SubstituteUtils;
import org.dawnsci.passerelle.cluster.service.ScalarNames;
import org.dawnsci.passerelle.cluster.service.SliceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import uk.ac.diamond.scisoft.analysis.message.DataMessageComponent;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

public class JobSliceSink extends Actor{
  private static final long serialVersionUID = -6014362686864092495L;
  private final static Logger LOGGER = LoggerFactory.getLogger(JobSliceSink.class);

  public Port input;

  /**
   * defines the file where the actor must write the job result slice definition
   */
  public StringParameter sliceFileParameter;

  public JobSliceSink(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, DataMessageComponent.class);
    sliceFileParameter = new StringParameter(this, "Slice file");
  }
  
  @Override
  public Logger getLogger() {
    return LOGGER;
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
    SliceBean slice = new SliceBean(
        dmc.getScalar(ScalarNames.DATASET), 
        dmc.getScalar(ScalarNames.SLICE), 
        dmc.getScalar(ScalarNames.SHAPE), 
        new File(dmc.getScalar(ScalarNames.FILEPATH)));
    try {
      writeSliceBean(getSliceFile(), slice);
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Failed to write slice "+slice, this, e);
    }
  }

  File getSliceFile() throws Exception {
    File sliceFile;
    String sliceFileStr = ParameterUtils.getSubstituedValue(sliceFileParameter);
    sliceFileStr = substituteSystemProps(sliceFileStr);
    sliceFile = new File(sliceFileStr);
    return sliceFile;
  }

  private String substituteSystemProps(String expression) {
    Properties properties = System.getProperties();
    Map<String,String> map = new HashMap<String,String>();
    for (final String name: properties.stringPropertyNames())
      map.put(name, properties.getProperty(name));
    
    return SubstituteUtils.substitute(expression, map);
  }

  private void writeSliceBean(File sliceFile, SliceBean sliceBean) {
    Properties props = sliceBean.toProperties();
    Writer writer = null;
    try {
      writer = new FileWriter(sliceFile);
      props.store(writer, null);
    } catch (Exception e) {
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (Exception e) {
        }
      }
    }
  }
}
