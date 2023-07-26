package com.bsren.qiu.jmx.l2;

public class Hello implements HelloMBean {


	String name;

	int age;

	public Hello(String name, int age){
		this.name = name;
		this.age = age;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getAge() {
		return age;
	}

	@Override
	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public void helloWorld() {
		System.out.println("hello");
	}
}
