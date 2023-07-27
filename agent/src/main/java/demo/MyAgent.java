package demo;

import java.lang.instrument.Instrumentation;

public class MyAgent {

	static Instrumentation instrumentation;

	public static void premain(String agentArgs, Instrumentation inst) throws Exception{
		instrumentation = inst;
		instrumentation.addTransformer(new MyClassFileTransformer());
	}
}
