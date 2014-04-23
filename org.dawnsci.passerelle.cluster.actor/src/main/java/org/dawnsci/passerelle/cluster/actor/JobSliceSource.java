package org.dawnsci.passerelle.cluster.actor;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dawb.passerelle.common.DatasetConstants;
import org.dawb.passerelle.common.message.IVariable;
import org.dawb.passerelle.common.message.IVariable.VARIABLE_TYPE;
import org.dawb.passerelle.common.message.IVariableProvider;
import org.dawb.passerelle.common.message.Variable;
import org.dawb.passerelle.common.parameter.ParameterUtils;
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
import com.isencia.passerelle.message.MessageException;

public class JobSliceSource extends Actor implements IVariableProvider {
  private static final long serialVersionUID = 3078492950853091843L;
  private final static Logger LOGGER = LoggerFactory.getLogger(JobSliceSink.class);

  public Port output;

  /**
   * defines the file where the actor must read the job's input slice definition
   */
  public StringParameter sliceFileParameter;

  public JobSliceSource(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);
    sliceFileParameter = new StringParameter(this, "Slice file");
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public List<IVariable> getInputVariables() {
    return Collections.emptyList();
  }

  @Override
  public List<IVariable> getOutputVariables() {
    final List<IVariable> ret = new ArrayList<>(5);
    ret.add(new Variable(ScalarNames.WORKINGDIR, VARIABLE_TYPE.SCALAR, "working directory", String.class));
    ret.add(new Variable(ScalarNames.DATASET, VARIABLE_TYPE.SCALAR, "dataset name", String.class));
    ret.add(new Variable(ScalarNames.SLICE, VARIABLE_TYPE.SCALAR, "slice in NCD compatible format", String.class));
    ret.add(new Variable(ScalarNames.FILENAME, VARIABLE_TYPE.SCALAR, "data file name", String.class));
    ret.add(new Variable(ScalarNames.FILEPATH, VARIABLE_TYPE.SCALAR, "path of the data file", String.class));

    return ret;
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      SliceBean sliceBean;
      File sliceFile = null;
      try {
        String sliceFileStr = substituteSystemProps(sliceFileParameter);
        sliceFile = new File(sliceFileStr);
        sliceBean = getOutputSlice(sliceFile);
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error getting slice file from parameter", this, e);
      }
      if (sliceBean != null) {
        ManagedMessage resultMsg = createMessage();
        final DataMessageComponent comp = new DataMessageComponent();
        comp.putScalar(ScalarNames.WORKINGDIR, sliceFile.getParent());
        comp.putScalar(ScalarNames.DATASET, sliceBean.getDataSet());
        comp.putScalar(ScalarNames.SLICE, sliceBean.getSlice());
        comp.putScalar(ScalarNames.SHAPE, sliceBean.getShape());
        comp.putScalar(ScalarNames.FILENAME, sliceBean.getFile().getName());
        comp.putScalar(ScalarNames.FILEPATH, sliceBean.getFile().getAbsolutePath());

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

  private String substituteSystemProps(StringParameter param) throws Exception {
    Properties properties = System.getProperties();
    Map<String, String> map = new HashMap<String, String>();
    for (final String name : properties.stringPropertyNames())
      map.put(name, properties.getProperty(name));

    return ParameterUtils.getSubstituedValue(param,null,map);
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
