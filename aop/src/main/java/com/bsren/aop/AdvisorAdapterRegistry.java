package com.bsren.aop;

import net.sf.cglib.proxy.MethodInterceptor;

public interface AdvisorAdapterRegistry {

	Advisor wrap(Object advice) throws Exception;

	MethodInterceptor[] getInterceptors(Advisor advisor) throws Exception;

	void registerAdvisorAdapter();

}
