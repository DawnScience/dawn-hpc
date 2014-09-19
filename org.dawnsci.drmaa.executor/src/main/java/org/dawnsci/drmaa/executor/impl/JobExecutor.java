/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.drmaa.executor.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @see JCIP 7.1.7, Listing 7.12
 * 
 * @author erwin
 *
 */
public class JobExecutor extends ThreadPoolExecutor {

  public JobExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }

  protected <T> java.util.concurrent.RunnableFuture<T> newTaskFor(java.util.concurrent.Callable<T> callable) {
    if(callable instanceof CancellableTask) {
      return ((CancellableTask<T>)callable).newFutureTask();
    } else {
      return super.newTaskFor(callable);
    }
  }
}
