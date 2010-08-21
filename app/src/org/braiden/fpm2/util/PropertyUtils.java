package org.braiden.fpm2.util;

/**
 * Copyright (c) 2010 Braiden Kindt
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public class PropertyUtils {

	private static final String METHOD_GET_CLASS = "getClass";
	private static final String GETTER_PREFIX = "get";
	private static final String GETTER_BOOLEAN_PREFIX = "is";
	private static final String SETTER_PREFIX = "set";
	
	private static final int GETTER = 0;
	private static final int SETTER = 1;
	
	public static boolean isReadable(Object bean, String property) {
		Validate.notNull(bean);
		Validate.notEmpty(property);
		return findMethod(bean.getClass(), property, GETTER) != null;
	}
	
	public static boolean isWriteable(Object bean, String property) {
		Validate.notNull(bean);
		Validate.notEmpty(property);
		return findMethod(bean.getClass(), property, SETTER) != null;
	}
	
	public static void setProperty(Object bean, String property, Object value) throws InvocationTargetException, IllegalAccessException {
		Validate.notNull(bean);
		Validate.notEmpty(property);
		Method method = findMethod(bean.getClass(), property, SETTER);
		if (method == null) {
			throw new InvocationTargetException(new NoSuchMethodException());
		}
		method.invoke(bean, value);		
	}
	
	public static Object getProperty(Object bean, String property) throws InvocationTargetException, IllegalAccessException {
		Validate.notNull(bean);
		Validate.notEmpty(property);
		Method method = findMethod(bean.getClass(), property, GETTER);
		if (method == null) {
			throw new InvocationTargetException(new NoSuchMethodException());
		}
		return method.invoke(bean);	
	}
	
	private static Method findMethod(Class<?> clazz, String property, int type) {
		Validate.notNull(clazz);
		Validate.isTrue(type == SETTER || type == GETTER);
		
		Method result = null;
		
		result = getMethod(clazz, GETTER_PREFIX + StringUtils.capitalize(property));
		if (result == null) {
			result = getMethod(clazz, GETTER_BOOLEAN_PREFIX + StringUtils.capitalize(property));
			result = result != null && (
					boolean.class.isAssignableFrom(result.getReturnType())
					|| Boolean.class.isAssignableFrom(result.getReturnType()))
					? result : null;
		}
		
		if (result != null && Void.class.isAssignableFrom(result.getReturnType())) {
			result = null;
		}
			
		if (result != null && type == SETTER) {
			result = getMethod(clazz, SETTER_PREFIX + StringUtils.capitalize(property), result.getReturnType());
		}
		
		return result;
	}
	
	private static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
		Method result = null;
		try {
			result = clazz.getMethod(methodName, params);
			
			if (result != null && !Modifier.isPublic(result.getModifiers())) {
				result = null;
			}
		} catch (NoSuchMethodException e) {
		
		}
		return result;
	}
	
	public static Map<String, Object> describe(Object bean) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Validate.notNull(bean);
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		for (Method method : bean.getClass().getMethods()) {
			boolean isAccepted = Modifier.isPublic(method.getModifiers()) 
					&& !Modifier.isStatic(method.getModifiers())
					&& method.getParameterTypes().length == 0
					&& !METHOD_GET_CLASS.equals(method.getName());
			
			boolean isObjGetter = isAccepted && !Void.class.isAssignableFrom(method.getReturnType())
					&& method.getName().startsWith(GETTER_PREFIX);
			boolean isBooleanGetter = isAccepted && (Boolean.class.isAssignableFrom(method.getReturnType())
					|| boolean.class.isAssignableFrom(method.getReturnType()))
					&& method.getName().startsWith(GETTER_BOOLEAN_PREFIX);
			
			if (isObjGetter || isBooleanGetter)
			{
				String propName = StringUtils.uncapitalize(
						isObjGetter
							? method.getName().substring(GETTER_PREFIX.length())
							: method.getName().substring(GETTER_BOOLEAN_PREFIX.length()));
				result.put(propName, method.invoke(bean));
			}
		}
		
		return result;
	}
	
}
