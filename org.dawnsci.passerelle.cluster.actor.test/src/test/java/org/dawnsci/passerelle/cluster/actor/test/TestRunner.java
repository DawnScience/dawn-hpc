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
