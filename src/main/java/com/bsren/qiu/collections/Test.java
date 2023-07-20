package com.bsren.qiu.collections;

public class Test {

	int age;

	String  name;

	Test t;

	Test(int age,String name){
		System.out.println(this);
		this.age = age;
		this.name = name;
		this.t = this;
		System.out.println(this);
	}

	public static void main(String[] args) {
		Test t = new Test(14,"rsb");
	}
}
