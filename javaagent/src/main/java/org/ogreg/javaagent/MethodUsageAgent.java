package org.ogreg.javaagent;


import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Objects;
import java.util.Properties;

public class MethodUsageAgent {

	public static void main(String[] args) throws Exception {
		String pid;
		if (args.length == 0) {
			pid = ProcessHandle.allProcesses()
					.filter(p -> p.info().commandLine().filter(c -> c.contains("org.ogreg.javaagent.SampleApp")).isPresent())
					.map(ProcessHandle::pid)
					.map(Objects::toString)
					.findFirst().orElseThrow();
		} else {
			pid = args[0];
		}

		String agentPath = new File("build/libs/mua.jar").getAbsolutePath(); // TODO determine dynamically

		System.err.printf("Attaching to %s (%s)...%n", pid, agentPath);

		VirtualMachine jvm = VirtualMachine.attach(pid);
		Properties props = jvm.getSystemProperties();
		System.err.println("Target VM options: " + props);

		jvm.loadAgent(agentPath);
		jvm.detach();

		System.err.println("Client exiting");
	}

	public static void premain(String agentArgs, Instrumentation inst) {
		System.err.println("MethodUsageAgent LOADED");
	}

	public static void agentmain(String agentArgs, Instrumentation inst) {
		System.err.println("MethodUsageAgent LOADED");
	}
}
