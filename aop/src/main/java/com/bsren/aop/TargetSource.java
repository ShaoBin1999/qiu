package com.bsren.aop;

import java.util.Objects;

public interface TargetSource extends TargetClassAware {

	@Override
	Class<?> getTargetClass();

	boolean isStatic();

	Object getTarget() throws Exception;

	void releaseTarget(Object target) throws Exception;
}
