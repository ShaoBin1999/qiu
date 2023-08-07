package com.bsren.aop;

public interface Advisor {

	Advice EMPTY_ADVICE = new Advice() {};

	Advice getAdvice();
}
