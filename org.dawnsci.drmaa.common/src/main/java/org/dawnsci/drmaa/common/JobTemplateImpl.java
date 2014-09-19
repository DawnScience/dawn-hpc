/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.drmaa.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.FileTransferMode;
import org.ggf.drmaa.InternalException;
import org.ggf.drmaa.InvalidAttributeValueException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.PartialTimestamp;
import org.ggf.drmaa.PartialTimestampFormat;
import org.ggf.drmaa.UnsupportedAttributeException;

/**
 * 
 * @author erwindl
 * 
 */
public class JobTemplateImpl implements JobTemplate {
  private static final String REMOTE_COMMAND = "drmaa_remote_command";
  private static final String INPUT_PARAMETERS = "drmaa_v_argv";
  private static final String JOB_SUBMISSION_STATE = "drmaa_js_state";
  private static final String JOB_ENVIRONMENT = "drmaa_v_env";
  private static final String WORKING_DIRECTORY = "drmaa_wd";
  private static final String JOB_CATEGORY = "drmaa_job_category";
  private static final String NATIVE_SPECIFICATION = "drmaa_native_specification";
  private static final String EMAIL_ADDRESS = "drmaa_v_email";
  private static final String BLOCK_EMAIL = "drmaa_block_email";
  private static final String START_TIME = "drmaa_start_time";
  private static final String JOB_NAME = "drmaa_job_name";
  private static final String INPUT_PATH = "drmaa_input_path";
  private static final String OUTPUT_PATH = "drmaa_output_path";
  private static final String ERROR_PATH = "drmaa_error_path";
  private static final String JOIN_FILES = "drmaa_join_files";
  private static final String TRANSFER_FILES = "drmaa_transfer_files";
  private static Set<String> supportedAttributeNames = new HashSet<>(Arrays.asList(REMOTE_COMMAND, INPUT_PARAMETERS, JOB_SUBMISSION_STATE, JOB_ENVIRONMENT,
      WORKING_DIRECTORY, JOB_CATEGORY, NATIVE_SPECIFICATION, EMAIL_ADDRESS, BLOCK_EMAIL, START_TIME, JOB_NAME, INPUT_PATH, OUTPUT_PATH, ERROR_PATH, JOIN_FILES,
      TRANSFER_FILES));

  /*
   * Not supported private static final String DEADLINE_TIME = "drmaa_deadline_time" private static final String
   * HARD_WALLCLOCK_TIME_LIMIT = "drmaa_wct_hlimit" private static final String SOFT_WALLCLOCK_TIME_LIMIT =
   * "drmaa_wct_slimit" private static final String HARD_RUN_DURATION_LIMIT = "drmaa_run_duration_hlimit" private static
   * final String SOFT_RUN_DURATION_LIMIT = "drmaa_run_duration_slimit"
   */
  private static final String HOLD_STRING = "drmaa_hold";
  private static final String ACTIVE_STRING = "drmaa_active";
  private static PartialTimestampFormat ptf = new PartialTimestampFormat();

  private UUID id;
  private String remoteCommand;
  private List<String> args = new ArrayList<>();
  private String stateString;
  private Map<String, String> env = new HashMap<>();
  private String workingDirectory;
  private String jobCategory;
  private String nativeSpecification;
  private Set<String> emailAddresses = new HashSet<>();
  private boolean blockEmail;
  private String startTime;
  private String jobName;
  private String inputPath;
  private String outputPath;
  private String errorPath;
  private boolean joinFiles;
  private String transferFiles;

  /**
   * Creates a new instance of JobTemplateImpl
   * 
   * @param session the associated SessionImpl object
   * @param uuid the table index of the native job template
   */
  public JobTemplateImpl(UUID uuid) {
    this.id = uuid;
  }

  public UUID getId() {
    return id;
  }

  public void setRemoteCommand(String remoteCommand) {
    this.remoteCommand = remoteCommand;
  }

  public String getRemoteCommand() {
    return remoteCommand;
  }

  public void setArgs(List<String> args) {
    this.args.clear();
    if(args!=null)
      this.args.addAll(args);
  }

  public List<String> getArgs() {
    return args;
  }

  public void setJobSubmissionState(int state) throws DrmaaException {
    if (state == HOLD_STATE) {
      stateString = HOLD_STRING;
    } else if (state == ACTIVE_STATE) {
      stateString = ACTIVE_STRING;
    } else {
      throw new InvalidAttributeValueException("jobSubmissionState attribute is invalid");
    }
  }

  public int getJobSubmissionState() throws DrmaaException {
    if ((stateString == null) || stateString.equals(ACTIVE_STRING)) {
      return ACTIVE_STATE;
    } else if (stateString.equals(HOLD_STRING)) {
      return HOLD_STATE;
    } else {
      /* This should never happen */
      throw new InternalException("jobSubmissionState property is unparsable");
    }
  }

  public void setJobEnvironment(Map<String, String> env) {
    this.env.clear();
    if(env!=null)
      this.env.putAll(env);
  }

  public Map<String, String> getJobEnvironment() {
    return env;
  }

  public void setWorkingDirectory(String wd) {
    this.workingDirectory = wd;
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  public void setJobCategory(String category) {
    this.jobCategory = category;
  }

  public String getJobCategory() {
    return jobCategory;
  }

  public void setNativeSpecification(String spec) {
    this.nativeSpecification = spec;
  }

  public String getNativeSpecification() {
    return nativeSpecification;
  }

  public void setEmail(Set<String> email) {
    this.emailAddresses.clear();
    if(email!=null)
      this.emailAddresses.addAll(email);
  }

  public Set<String> getEmail() {
    return emailAddresses;
  }

  public void setBlockEmail(boolean blockEmail) {
    this.blockEmail = blockEmail;
  }

  public boolean getBlockEmail() {
    return this.blockEmail;
  }

  public void setStartTime(PartialTimestamp startTime) {
    this.startTime = ptf.format(startTime);
  }

  public PartialTimestamp getStartTime() throws DrmaaException {
    if (startTime != null) {
      try {
        return ptf.parse(this.startTime);
      } catch (java.text.ParseException e) {
        throw new InternalException("startTime property is unparsable");
      }
    } else {
      return null;
    }
  }

  public void setJobName(String name) {
    this.jobName = name;
  }

  public String getJobName() {
    return jobName;
  }

  public void setInputPath(String inputPath) {
    this.inputPath = inputPath;
  }

  public String getInputPath() {
    return inputPath;
  }

  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

  public String getOutputPath() {
    return outputPath;
  }

  public void setErrorPath(String errorPath) {
    this.errorPath = errorPath;
  }

  public String getErrorPath() {
    return errorPath;
  }

  public void setJoinFiles(boolean join) {
    this.joinFiles = join;
  }

  public boolean getJoinFiles() {
    return joinFiles;
  }

  public void setTransferFiles(FileTransferMode mode) {
    if (mode != null) {
      StringBuffer buf = new StringBuffer();
      if (mode.getInputStream()) {
        buf.append('i');
      }
      if (mode.getOutputStream()) {
        buf.append('o');
      }
      if (mode.getErrorStream()) {
        buf.append('e');
      }
      this.transferFiles = buf.toString();
    } else {
      this.transferFiles = null;
    }
  }

  public FileTransferMode getTransferFiles() {
    if (transferFiles != null) {
      return new FileTransferMode((transferFiles.indexOf('i') != -1), (transferFiles.indexOf('o') != -1), (transferFiles.indexOf('e') != -1));
    } else {
      return null;
    }
  }

  /**
   * Unsupported property. Will throw an UnsupportedAttributeException if called.
   * 
   * @throws UnsupportedAttributeException unsupported property
   */
  public void setDeadlineTime(PartialTimestamp deadline) throws UnsupportedAttributeException {
    throw new UnsupportedAttributeException("The deadlineTime attribute " + "is not supported.");
  }

  /**
   * Unsupported property. Will throw an UnsupportedAttributeException if called.
   * 
   * @throws UnsupportedAttributeException unsupported property
   */
  public PartialTimestamp getDeadlineTime() throws UnsupportedAttributeException {
    throw new UnsupportedAttributeException("The deadlineTime attribute " + "is not supported.");
  }

  /**
   * Unsupported property. Will throw an UnsupportedAttributeException if called.
   * 
   * @throws UnsupportedAttributeException unsupported property
   */
  public void setHardWallclockTimeLimit(long hardWallclockLimit) throws UnsupportedAttributeException {
    throw new UnsupportedAttributeException("The hardWallclockTimeLimit " + "attribute is not supported.");
  }

  /**
   * Unsupported property. Will throw an UnsupportedAttributeException if called.
   * 
   * @throws UnsupportedAttributeException unsupported property
   */
  public long getHardWallclockTimeLimit() throws UnsupportedAttributeException {
    throw new UnsupportedAttributeException("The hardWallclockTimeLimit " + "attribute is not supported.");
  }

  /**
   * Unsupported property. Will throw an UnsupportedAttributeException if called.
   * 
   * @throws UnsupportedAttributeException unsupported property
   */
  public void setSoftWallclockTimeLimit(long softWallclockLimit) throws UnsupportedAttributeException {
    throw new UnsupportedAttributeException("The softWallclockTimeLimit " + "attribute is not supported.");
  }

  /**
   * Unsupported property. Will throw an UnsupportedAttributeException if called.
   * 
   * @throws UnsupportedAttributeException unsupported property
   */
  public long getSoftWallclockTimeLimit() throws UnsupportedAttributeException {
    throw new UnsupportedAttributeException("The softWallclockTimeLimit " + "attribute is not supported.");
  }

  /**
   * Unsupported property. Will throw an UnsupportedAttributeException if called.
   * 
   * @throws UnsupportedAttributeException unsupported property
   */
  public void setHardRunDurationLimit(long hardRunLimit) throws UnsupportedAttributeException {
    throw new UnsupportedAttributeException("The hardRunDurationLimit " + "attribute is not supported.");
  }

  /**
   * Unsupported property. Will throw an UnsupportedAttributeException if called.
   * 
   * @throws UnsupportedAttributeException unsupported property
   */
  public long getHardRunDurationLimit() throws UnsupportedAttributeException {
    throw new UnsupportedAttributeException("The hardRunDurationLimit " + "attribute is not supported.");
  }

  /**
   * Unsupported property. Will throw an UnsupportedAttributeException if called.
   * 
   * @throws UnsupportedAttributeException unsupported property
   */
  public void setSoftRunDurationLimit(long softRunLimit) throws UnsupportedAttributeException {
    throw new UnsupportedAttributeException("The softRunDurationLimit " + "attribute is not supported.");
  }

  /**
   * Unsupported property. Will throw an UnsupportedAttributeException if called.
   * 
   * @throws UnsupportedAttributeException unsupported property
   */
  public long getSoftRunDurationLimit() throws UnsupportedAttributeException {
    throw new UnsupportedAttributeException("The softRunDurationLimit " + "attribute is not supported.");
  }

  /**
   * Returns the list of supported properties names. With the execd param, delegated_file_staging, set to false, this
   * list includes only the list of DRMAA required properties. With delegated_file_staging set to true, the list also
   * includes the transferFiles property.</p>
   * 
   * @return {@inheritDoc}
   */
  public Set<String> getAttributeNames() {
    return supportedAttributeNames;
  }

  /**
   * Tests whether this JobTemplateImpl represents the same native job template as the given object. This implementation
   * means that even if two JobTemplateImpl instance's have all the same settings, they are not equal, because they are
   * associated with different native job templates.
   * 
   * @param obj the object against which to compare
   * @return whether the the given object is the same as this object
   */
  public boolean equals(Object obj) {
    if (obj instanceof JobTemplateImpl) {
      return (this.getId() == ((JobTemplateImpl) obj).getId());
    } else {
      return false;
    }
  }

  /**
   * Returns a hash code based on the associated native job template's table index.
   * 
   * @return the hash code
   */
  public int hashCode() {
    return this.getId().hashCode();
  }

  @Override
  public String toString() {
    return "JobTemplate [id=" + id + ", jobName=" + jobName + ", remoteCommand=" + remoteCommand + "]";
  }
}
