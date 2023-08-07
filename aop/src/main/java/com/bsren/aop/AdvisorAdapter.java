package com.bsren.aop;

import net.sf.cglib.proxy.MethodInterceptor;

public interface AdvisorAdapter {

	boolean supportAdvice(Advice advice);

	MethodInterceptor getInterceptor(Advisor advisor);
}
