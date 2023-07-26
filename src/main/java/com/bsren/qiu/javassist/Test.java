package com.bsren.qiu.javassist;

import java.sql.SQLException;
import java.sql.Wrapper;

public class Test implements Wrapper {

	static class T extends Test{

	}



	public static void main(String[] args) throws SQLException {
	}


	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return iface.cast(this);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this);
	}


	class A implements B{

		@Override
		public void func() {

		}
	}

	interface B{
		void func();
	}
}