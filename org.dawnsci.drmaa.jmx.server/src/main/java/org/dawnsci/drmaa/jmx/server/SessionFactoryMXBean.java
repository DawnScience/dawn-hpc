package org.dawnsci.drmaa.jmx.server;


public interface SessionFactoryMXBean {
  
  SessionMXBean getSession() throws Exception;

}
