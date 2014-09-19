/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.drmaa.executor.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;

/**
 * @see JCIP 7.1.7, Listing 7.12
 * 
 * @author erwin
 */
public interface CancellableTask<T> extends Callable<T> {
  void cancel();
  RunnableFuture<T> newFutureTask();
}
