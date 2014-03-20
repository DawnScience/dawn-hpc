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
package org.dawnsci.drmaa.executor.impl;

import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author erwindl
 *
 */
public class JobExecutionFuture extends FutureTask<Integer> {
  private final static Logger LOGGER = LoggerFactory.getLogger(JobExecutionFuture.class);

  private JobExecutionTask jet;

  public JobExecutionFuture(JobExecutionTask jet) {
    super(jet);
    this.jet = jet;
  }
  
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    try {
      jet.cancel();
      LOGGER.info("Cancelled job "+jet);
    } catch (Throwable t) {
      LOGGER.error("Failed to cancel Job "+jet,t);
    }
    return super.cancel(mayInterruptIfRunning);
  }
  
  public String getId() {
    return jet.getId();
  }
}
