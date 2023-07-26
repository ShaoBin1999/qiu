package com.bsren.qiu.javassist;


import javassist.*;
import javassist.bytecode.ClassFile;

import java.sql.*;
import java.util.*;

public class JavassistProxyFactory {

	private static ClassPool classPool;
	private static String genDirectory = "";


	class A implements C{

		@Override
		public void func() {
			System.out.println("a");
		}
	}

	class B extends A{
		@Override
		public void func() {
			System.out.println("b");
		}
	}

	interface C{
		void func();
	}

	public static void main(String[] args) throws Exception {


		classPool = new ClassPool();
		classPool.importPackage("java.sql");
		classPool.appendClassPath(new LoaderClassPath(JavassistProxyFactory.class.getClassLoader()));
		String methodBody = "{ try { return delegate.method($$); } catch (SQLException e) { throw checkException(e); } }";
		generateProxyClass(C.class,A.class.getName(),methodBody);
	}

	private static <T> void generateProxyClass(Class<T> primaryInterface, String superClassName, String methodBody) throws Exception
	{
		String newClassName = superClassName.replaceAll("(.+)\\.(\\w+)", "$1.Hikari$2");

		CtClass superCt = classPool.getCtClass(superClassName);
		CtClass targetCt = classPool.makeClass(newClassName, superCt);
		targetCt.setModifiers(Modifier.FINAL);

		System.out.println("Generating " + newClassName);

		targetCt.setModifiers(Modifier.PUBLIC);

		// Make a set of method signatures we inherit implementation for, so we don't generate delegates for these
		Set<String> superSigs = new HashSet<>();
		for (CtMethod method : superCt.getMethods()) {
			if ((method.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
				superSigs.add(method.getName() + method.getSignature());
			}
		}

		Set<String> methods = new HashSet<>();
		Set<Class<?>> interfaces = getAllInterfaces(primaryInterface);
		for (Class<?> intf : interfaces) {
			CtClass intfCt = classPool.getCtClass(intf.getName());
			targetCt.addInterface(intfCt);
			for (CtMethod intfMethod : intfCt.getDeclaredMethods()) {
				final String signature = intfMethod.getName() + intfMethod.getSignature();

				// don't generate delegates for methods we override
				if (superSigs.contains(signature)) {
					continue;
				}

				// Ignore already added methods that come from other interfaces
				if (methods.contains(signature)) {
					continue;
				}

				// Track what methods we've added
				methods.add(signature);

				// Clone the method we want to inject into
				CtMethod method = CtNewMethod.copy(intfMethod, targetCt, null);

				String modifiedBody = methodBody;

				// If the super-Proxy has concrete methods (non-abstract), transform the call into a simple super.method() call
				CtMethod superMethod = superCt.getMethod(intfMethod.getName(), intfMethod.getSignature());
				if ((superMethod.getModifiers() & Modifier.ABSTRACT) != Modifier.ABSTRACT && !isDefaultMethod(intf, intfCt, intfMethod)) {
					modifiedBody = modifiedBody.replace("((cast) ", "");
					modifiedBody = modifiedBody.replace("delegate", "super");
					modifiedBody = modifiedBody.replace("super)", "super");
				}

				modifiedBody = modifiedBody.replace("cast", primaryInterface.getName());

				// Generate a method that simply invokes the same method on the delegate
				if (isThrowsSqlException(intfMethod)) {
					modifiedBody = modifiedBody.replace("method", method.getName());
				}
				else {
					modifiedBody = "{ return ((cast) delegate).method($$); }".replace("method", method.getName()).replace("cast", primaryInterface.getName());
				}

				if (method.getReturnType() == CtClass.voidType) {
					modifiedBody = modifiedBody.replace("return", "");
				}

				method.setBody(modifiedBody);
				targetCt.addMethod(method);
			}
		}

		targetCt.getClassFile().setMajorVersion(ClassFile.JAVA_8);
		targetCt.writeFile(genDirectory + "target/classes");
	}

	private static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
		Set<Class<?>> interfaces = new HashSet<>();
		for (Class<?> intf : clazz.getInterfaces()) {
			if (intf.getInterfaces().length > 0) {
				interfaces.addAll(getAllInterfaces(intf));
			}
			interfaces.add(intf);
		}
		if (clazz.getSuperclass() != null) {
			interfaces.addAll(getAllInterfaces(clazz.getSuperclass()));
		}

		if (clazz.isInterface()) {
			interfaces.add(clazz);
		}

		return interfaces;
	}


	private static boolean isThrowsSqlException(CtMethod method) {
		try {
			for (CtClass clazz : method.getExceptionTypes()) {
				if (clazz.getSimpleName().equals("SQLException")) {
					return true;
				}
			}
		}
		catch (NotFoundException ignored) {
		}

		return false;
	}

	private static boolean isDefaultMethod(Class<?> intf, CtClass intfCt, CtMethod intfMethod) throws Exception
	{
		List<Class<?>> paramTypes = new ArrayList<>();

		for (CtClass pt : intfMethod.getParameterTypes()) {
			paramTypes.add(toJavaClass(pt));
		}

		return intf.getDeclaredMethod(intfMethod.getName(), paramTypes.toArray(new Class[paramTypes.size()])).toString().contains("default ");
	}


	private static Class<?> toJavaClass(CtClass cls) throws Exception
	{
		if (cls.getName().endsWith("[]")) {
			return java.lang.reflect.Array.newInstance(toJavaClass(cls.getName().replace("[]", "")), 0).getClass();
		}
		else {
			return toJavaClass(cls.getName());
		}
	}

	private static Class<?> toJavaClass(String cn) throws Exception
	{
		switch (cn) {
			case "int":
				return int.class;
			case "long":
				return long.class;
			case "short":
				return short.class;
			case "byte":
				return byte.class;
			case "float":
				return float.class;
			case "double":
				return double.class;
			case "boolean":
				return boolean.class;
			case "char":
				return char.class;
			case "void":
				return void.class;
			default:
				return Class.forName(cn);
		}
	}
}
