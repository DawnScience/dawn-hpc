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
package org.dawnsci.passerelle.cluster.actor.activator;

import org.dawnsci.passerelle.cluster.actor.ClusterNodeTransformer;
import org.dawnsci.passerelle.cluster.actor.JobSliceSink;
import org.dawnsci.passerelle.cluster.actor.JobSliceSource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.ext.impl.DefaultModelElementClassProvider;

public class Activator implements BundleActivator {

	private static BundleContext context;
  private ServiceRegistration apSvcReg;

	static BundleContext getContext() {
		return context;
	}

  private BundleActivator testFragmentActivator;

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
    apSvcReg = context.registerService(ModelElementClassProvider.class.getName(), 
        new DefaultModelElementClassProvider(
            ClusterNodeTransformer.class,
            JobSliceSource.class,
            JobSliceSink.class
            ), null);
		
    try {
      Class<? extends BundleActivator> svcTester = 
          (Class<? extends BundleActivator>) Class.forName("org.dawnsci.passerelle.cluster.actor.activator.TestFragmentActivator");
      testFragmentActivator = svcTester.newInstance();
      testFragmentActivator.start(context);
    } catch (ClassNotFoundException e) {
      // ignore, means the test fragment is not present...
      // it's a dirty way to find out, but don't know how to discover fragment contribution in a better way...
    }
	}

	public void stop(BundleContext bundleContext) throws Exception {
    apSvcReg.unregister();
		Activator.context = null;
    if (testFragmentActivator != null) {
      testFragmentActivator.stop(context);
    }
	}
}
