package com.bsren.aop;

public interface Advised extends TargetClassAware{

	Advice EMPTY_ADVICE = new Advice() {};

	Advice getAdvice();
}
