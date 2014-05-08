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
package org.dawnsci.passerelle.cluster.actor.test;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

public class TestRunner implements CommandProvider {

	public String getHelp() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("\n---Cluster Actor test---\n");
		buffer.append("\trunClusterActorTest\n");
		return buffer.toString();
	}

	public void _runClusterActorTest(CommandInterpreter ci) {
		junit.textui.TestRunner.run(TestClusterActor.class);
	}
}
