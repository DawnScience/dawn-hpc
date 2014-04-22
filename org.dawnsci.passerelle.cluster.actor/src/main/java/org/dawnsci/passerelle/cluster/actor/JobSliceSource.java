package org.dawnsci.passerelle.cluster.actor;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

import org.dawb.passerelle.common.DatasetConstants;
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
import com.isencia.passerelle.message.MessageException;

public class JobSliceSource extends Actor {
  private static final long serialVersionUID = 3078492950853091843L;
  private final static Logger LOGGER = LoggerFactory.getLogger(JobSliceSink.class);

  public Port output;

  /**
   * defines the file where the actor must read the job's input slice definition
   */
  public FileParameter sliceInputFileParameter;

  public JobSliceSource(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);
    sliceInputFileParameter = new FileParameter(this, "Slice file");
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      SliceBean sliceBean;
      File sliceFile = null;
      try {
        sliceFile = sliceInputFileParameter.asFile();
        sliceBean = getOutputSlice(sliceFile);
      } catch (IllegalActionException e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error getting slice file from parameter", this, e);
      }
      if (sliceBean != null) {
        ManagedMessage resultMsg = createMessage();
        final DataMessageComponent comp = new DataMessageComponent();
        comp.putScalar("dataSet", sliceBean.getDataSet());
        comp.putScalar("slice", sliceBean.getSlice());
        comp.putScalar("shape", sliceBean.getShape());
        comp.putScalar("file_name", sliceBean.getFile().getName());
        comp.putScalar("file_path", sliceBean.getFile().getAbsolutePath());

        try {
          resultMsg.setBodyContent(comp, DatasetConstants.CONTENT_TYPE_DATA);
          response.addOutputMessage(output, resultMsg);
        } catch (MessageException e) {
          response.setException(new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Failed to create result msg for " + sliceBean, this, e));
        }
      } else {
        getLogger().warn("No slice found in " + sliceFile);
      }
    } finally {
      requestFinish();
    }
  }

  private SliceBean getOutputSlice(File sliceFile) {
    Properties props = new Properties();
    if (sliceFile.exists()) {
      Reader sliceReader = null;
      try {
        sliceReader = new FileReader(sliceFile);
        props.load(sliceReader);
        return SliceBean.fromProperties(props);
      } catch (Exception e) {
        getLogger().error("Error reading slice from " + sliceFile, e);
      } finally {
        if (sliceReader != null) {
          try {
            sliceReader.close();
          } catch (Exception e) {
          }
        }
      }
    }
    return null;
  }
}
