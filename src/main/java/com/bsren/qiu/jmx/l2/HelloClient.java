package com.bsren.qiu.jmx.l2;

import java.io.IOException;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


public class HelloClient {
	public static void main(String[] args) throws IOException, Exception, NullPointerException {
		JMXServiceURL url = new JMXServiceURL
				("service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi");
		JMXConnector jmxc = JMXConnectorFactory.connect(url,null);

		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
		//ObjectName的名称与前面注册时候的保持一致
		ObjectName mbeanName = new ObjectName("jmxBean:name=hello");

		System.out.println("Domains ......");
		String[] domains = mbsc.getDomains();

		for(int i=0;i<domains.length;i++) {
			System.out.println("domain[" + i + "]=" + domains[i] );
		}

		System.out.println("MBean count = " + mbsc.getMBeanCount());
		//设置指定Mbean的特定属性值
		//这里的setAttribute、getAttribute操作只能针对bean的属性
		//例如对getName或者setName进行操作，只能使用Name，需要去除方法的前缀
		mbsc.setAttribute(mbeanName, new Attribute("Name","王若晴"));
		mbsc.setAttribute(mbeanName, new Attribute("Age",22));
		Integer age = (Integer) mbsc.getAttribute(mbeanName, "Age");
		String name = (String)mbsc.getAttribute(mbeanName, "Name");
		System.out.println("age=" + age + ";name=" + name);

		HelloMBean proxy = MBeanServerInvocationHandler.
				newProxyInstance(mbsc, mbeanName, HelloMBean.class, false);
		proxy.helloWorld();
	}
}