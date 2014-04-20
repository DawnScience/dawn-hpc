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

package org.dawnsci.drmaa.jmx.connection;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.dawnsci.drmaa.jmx.SessionFactoryMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author erwindl
 * 
 */
public class ConnectionManager {
  private final static Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

  private static final String REMOTE_DRMAA_SESSIONFACTORY_MXBEAN_NAME = "org.dawnsci.drmaa:type=SessionFactory";
  private static final String DRMAA_JMX_HOST_NAME_PROPERTY_NAME = "org.dawnsci.drmaa.jmx.host.name";
  private static final String DEFAULT_PORT = "28000";
  private static final String DRMAA_JMX_PORT_PROPERTY_NAME = "org.dawnsci.drmaa.jmx.port";
  private static final String DRMAA_GATEWAY_URL_PATH = "DrmaaGateway";

  private ObjectName sessionFactoryMxbeanName;

  private int port;
  private String hostName;
  private JMXServiceURL serverUrl;

  private JMXConnectorServer rmiConnector;

  /**
   * 
   * @throws UnknownHostException if the configured host name of the DRMAA submission gateway is unknown
   */
  public ConnectionManager() throws UnknownHostException {
    try {
      sessionFactoryMxbeanName = new ObjectName(REMOTE_DRMAA_SESSIONFACTORY_MXBEAN_NAME);
    } catch (MalformedObjectNameException e) {
      throw new RuntimeException(e);
    }
    port = Integer.parseInt(System.getProperty(DRMAA_JMX_PORT_PROPERTY_NAME, DEFAULT_PORT));
    hostName = System.getProperty(DRMAA_JMX_HOST_NAME_PROPERTY_NAME);
    if (hostName == null)
      hostName = InetAddress.getLocalHost().getHostName();
    if (hostName == null)
      hostName = InetAddress.getLocalHost().getHostAddress();
    if (hostName == null)
      hostName = "localhost";

    InetAddress.getByName(hostName);
  }

  public JMXServiceURL getConnectionURL() throws MalformedURLException {
    if (serverUrl == null) {
      serverUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName + ":" + port + "/"+DRMAA_GATEWAY_URL_PATH);
    }
    return serverUrl;
  }

  public ObjectName getSessionFactoryMxBeanName() {
    return sessionFactoryMxbeanName;
  }

  /**
   * Server-side utility to start an RMI connector for the JMX-based DRMAA SessionFactory
   * 
   * @throws IOException
   * @throws NotCompliantMBeanException
   * @throws MBeanRegistrationException
   * @throws InstanceAlreadyExistsException
   */
  public void startSessionFactoryMXBean(SessionFactoryMXBean sessionFactory) throws IOException, InstanceAlreadyExistsException, MBeanRegistrationException,
      NotCompliantMBeanException {

    MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
    platformMBeanServer.registerMBean(sessionFactory, sessionFactoryMxbeanName);

    try {
      // start an RMI registry on the right port
      LocateRegistry.createRegistry(port);
    } catch (java.rmi.server.ExportException ne) {
      LOGGER.debug("Found existing registry on " + port);
    }
    rmiConnector = JMXConnectorServerFactory.newJMXConnectorServer(getConnectionURL(), null, platformMBeanServer);
    rmiConnector.start();

    LOGGER.info("Activated {}", sessionFactoryMxbeanName);
  }

  public void stopSessionFactoryMXBean(SessionFactoryMXBean sessionFactory) {
    if (rmiConnector != null) {
      try {
        rmiConnector.stop();
      } catch (IOException e) {
        // ignore
      }
    }
    if (sessionFactoryMxbeanName != null) {
      try {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(sessionFactoryMxbeanName);
      } catch (MBeanRegistrationException | InstanceNotFoundException e) {
        // ignore
      }

      LOGGER.info("Deactivated {}", sessionFactoryMxbeanName);
    }
  }

  /**
   * Client-side utility method to obtain a connection to the remote DRMAA SessionFactory via JMX.
   * 
   * @param timeout
   * @return
   * @throws Exception
   */
  public MBeanServerConnection getServerConnection(final long timeout) throws Exception {
    long waited = 0;
    MBeanServerConnection server = null;

    while (timeout > waited) {
      waited += 100;
      try {
        JMXConnector conn = JMXConnectorFactory.connect(getConnectionURL());
        server = conn.getMBeanServerConnection();
        break;
      } catch (Throwable ne) {
        if (waited >= timeout) {
          throw new Exception("Cannot get connection", ne);
        } else {
          Thread.sleep(100);
          continue;
        }
      }
    }
    return server;
  }
}
