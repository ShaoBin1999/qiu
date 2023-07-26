package com.bsren.qiu.jmx.l1;

import javax.management.*;
import java.lang.management.ManagementFactory;

public class HelloAgent {

	public static void main(String[] args) throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, MalformedObjectNameException, InterruptedException {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		ObjectName helloName = new ObjectName("jmxBean:name=hello");
		mBeanServer.registerMBean(new Hello("rsb",24),helloName);
		Thread.sleep(1000*6000*60);
	}
}
