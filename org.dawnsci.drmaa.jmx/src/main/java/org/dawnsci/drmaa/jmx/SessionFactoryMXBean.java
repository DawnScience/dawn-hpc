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
package org.dawnsci.drmaa.jmx;

/**
 * The interface for a JMX-based remote access to a DRMAA SessionFactory.
 * 
 * @author erwindl
 *
 */
public interface SessionFactoryMXBean {
  
  /**
   * In order to follow JMX conventions about getters&setters, 
   * the getSession method as defined in drmaa's SessionFactory
   * is renamed to createSession here.
   * 
   * @return
   * @throws Exception
   */
  SessionMXBean createSession() throws Exception;

}
