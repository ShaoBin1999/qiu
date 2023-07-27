package demo;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class MyClassFileTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		System.out.println("Transforming " + className);
		//非自有类, 直接返回
		if (!className.startsWith("top/soft1010")) {
			return classfileBuffer;
		}
		//为null,表示由bootstrapLoader加载的,不能重写
		if (loader == null) {
			return classfileBuffer;
		}
		try {
//            if (className != null && className.indexOf("/") != -1) {
//                className = className.replaceAll("/", ".");
//            }
			CtClass ctClass = ClassPool.getDefault().makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
			CtMethod[] methods = ctClass.getDeclaredMethods();
			for (CtMethod method : methods) {
				try {
					method.insertAfter("System.out.println(\"after\");");
					method.insertBefore("System.out.println(\"before\");");
				} catch (CannotCompileException e) {
					e.printStackTrace();
				}
			}
			return ctClass.toBytecode();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classfileBuffer;
	}
}
