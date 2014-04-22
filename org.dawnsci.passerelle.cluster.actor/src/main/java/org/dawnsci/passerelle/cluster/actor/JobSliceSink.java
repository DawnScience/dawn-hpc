package org.dawnsci.passerelle.cluster.actor;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Properties;

import org.dawb.passerelle.common.message.MessageUtils;
import org.dawnsci.passerelle.cluster.service.SliceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.FileParameter;
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
  public FileParameter sliceResultFileParameter;

  public JobSliceSink(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, DataMessageComponent.class);
    sliceResultFileParameter = new FileParameter(this, "Slice file");
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
    SliceBean slice = new SliceBean(dmc.getScalar("dataSet"), dmc.getScalar("slice"), dmc.getScalar("shape"), new File(dmc.getScalar("file_path")));
    try {
      writeSliceBean(sliceResultFileParameter.asFile(), slice);
    } catch (IllegalActionException e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Failed to write slice "+slice, this, e);
    }
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
