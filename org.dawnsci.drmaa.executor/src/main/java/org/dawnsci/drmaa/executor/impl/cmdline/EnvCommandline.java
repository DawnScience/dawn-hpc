/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.drmaa.executor.impl.cmdline;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the <code>Commandline</code> class to provide a means to manipulate
 * the OS environment under which the command will run.
 * 
 * @author <a href="mailto:rjmpsmith@hotmail.com">Robert J. Smith</a>
 */
public class EnvCommandline extends Commandline {

  private static final Logger LOG = LoggerFactory.getLogger(EnvCommandline.class);

  /**
   * Provides the OS environment under which the command will run.
   */
  private OSEnvironment env = new OSEnvironment();

  /**
   * Constructor which takes a command line string and attempts to parse it into
   * it's various components.
   * 
   * @param command The command
   */
  public EnvCommandline(String command) {
    super(command);
  }

  /**
   * Default constructor
   */
  public EnvCommandline() {
    super();
  }

  /**
   * Sets a variable within the environment under which the command will be run.
   * 
   * @param var The environment variable to set
   * @param value The value of the variable
   */
  public void setVariable(String var, String value) {
    env.add(var, value);
  }

  /**
   * Gets the value of an environment variable. The variable name is case
   * sensitive.
   * 
   * @param var The variable for which you wish the value
   * @return The value of the variable, or <code>null</code> if not found
   */
  public String getVariable(String var) {
    return env.getVariable(var);
  }

  /**
   * Executes the command.
   */
  public Process execute() throws IOException {
    Process process;

    // Let the user know what's happening
    File workingDir = getWorkingDir();
    if (workingDir == null) {
      LOG.debug("Executing \"" + this + "\"");
      process = Runtime.getRuntime().exec(getCommandline(), env.toArray());
    } else {
      LOG.debug("Executing \"" + this + "\" in directory " + workingDir.getAbsolutePath());
      process = Runtime.getRuntime().exec(getCommandline(), env.toArray(), workingDir);
    }

    return process;
  }
}
