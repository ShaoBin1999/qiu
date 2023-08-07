package com.bsren.aop;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

public class DefaultAdvisorChainFactory extends AdvisorChainFactory implements Serializable {

	@Override
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method, Class<?> targetClass) {
		return null;
	}
}
