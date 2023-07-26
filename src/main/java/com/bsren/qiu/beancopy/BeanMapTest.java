package com.bsren.qiu.beancopy;

import net.sf.cglib.beans.BeanCopier;
import net.sf.cglib.beans.BeanMap;
import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
public class BeanMapTest {

	String name;

	Integer age;

	public BeanMapTest(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	static class P{
		String name;

		Integer age;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		@Override
		public String toString() {
			return name+" "+age;
		}
	}

	public static void main(String[] args) {
		BeanMapTest t = new BeanMapTest("rsb",24);
		P p = copyV2(t, P.class);
		System.out.println(p);

	}

	public static <S, T> T copyV2(S source, Class<T> targetClass) {
		if (Objects.isNull(source)) {
			return null;
		}
		try {
			//记录source对象中的值到map中
			Map<String, Object> sourceMap = beanToMap(source);
			//创建目标实例
			T target = targetClass.newInstance();
			//性能优化
			Field[] fields = getBeanFields(targetClass);
			for (Field targetField : fields) {
				if ("serialVersionUID".equals(targetField.getName())) {
					continue;
				}
				//获取目标属性上的注解
				//根据不同的策略进行对原实体的属性名称进行修改
				targetField.setAccessible(true);
				//将值拷贝到目标实体
				Object value = smartTypeMapping(sourceMap, targetField);
				if (Objects.nonNull(value)) {
					targetField.set(target, smartTypeMapping(sourceMap,targetField));
				}
			}
			return target;
		} catch (InstantiationException | IllegalAccessException e) {
		}
		return null;
	}


	private static Object smartTypeMapping(Map<String, Object> sourceMap, Field field)
			throws IllegalAccessException {
		Object valueObj;
		if (field.getType() == List.class) {
			return Lists.newArrayList();
		} else {
			valueObj = sourceMap.getOrDefault(field.getName(), null);
			if (valueObj != null && field.getType() != valueObj.getClass()) {
				valueObj = null;
			}
		}
		if (valueObj == null) {
			return null;
		}

		// 两者类型相同，直接返回
		if (valueObj.getClass() == field.getType()) {
			return valueObj;
		}

		String strVal = valueObj.toString();
		if (field.getType() == String.class) {
			return strVal;
		}
		if (StringUtils.isBlank(strVal)) {
			return null;
		}
		if (field.getType() == Integer.class) {
			return Integer.parseInt(strVal);
		}
		if (field.getType() == Long.class) {
			return Long.parseLong(strVal);
		}
		if (field.getType() == Date.class) {
			return null;  //
		}
		return valueObj;
	}



	public static Map<String, Object> beanToMap(Object bean) {
		return null == bean ? null : BeanMap.create(bean);
	}

	private static final Map<Identity, BeanCopier> BEAN_COPIER_MAP = new ConcurrentHashMap<>();
	private static final Map<Identity, Field[]> BEAN_FIELD_MAP = new ConcurrentHashMap<>();


	public static <S, T> Field[] getBeanFields(Class<T> targetClass) {
		Identity key = genKey(targetClass, targetClass);
		Field[] fields;
		if (BEAN_FIELD_MAP.containsKey(key)) {
			fields = BEAN_FIELD_MAP.get(key);
		} else {
			fields = targetClass.getDeclaredFields();
			BEAN_FIELD_MAP.put(key, fields);
		}
		return fields;
	}

	private static <S, T> Identity genKey(Class<S> sourceClazz, Class<T> targetClazz) {
		return new Identity(sourceClazz.getName(), targetClazz.getName());
	}



	private static class Identity {
		private String source;
		private String target;

		public Identity(String source, String target) {
			this.source = source;
			this.target = target;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}
	}
}
