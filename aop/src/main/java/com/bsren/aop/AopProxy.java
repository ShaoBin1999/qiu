package com.bsren.aop;

import java.util.Objects;

public interface AopProxy {

	Object getProxy();

	Object getProxy(ClassLoader classLoader);
}
