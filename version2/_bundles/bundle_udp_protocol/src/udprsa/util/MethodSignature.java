package udprsa.util;

import java.lang.reflect.Method;

/*
 * Utility class for creating a method signature String from Method object
 */
public class MethodSignature {

	public static final int VOID = 0;
	public static final int BOOLEAN = 1;
	public static final int CHAR = 2;
	public static final int BYTE = 3;
	public static final int SHORT = 4;
	public static final int INT = 5;
	public static final int FLOAT = 6;
	public static final int LONG = 7;
	public static final int DOUBLE = 8;
	public static final int ARRAY = 9;
	public static final int OBJECT = 10;

	public static String getMethodSignature(final Method m) {
		Class[] parameters = m.getParameterTypes();
		StringBuffer buf = new StringBuffer();
		buf.append(m.getName());
		buf.append('(');
		for (int i = 0; i < parameters.length; ++i) {
			getDescriptor(buf, parameters[i]);
		}
		buf.append(')');
		getDescriptor(buf, m.getReturnType());
		return buf.toString();
	}

	private static void getDescriptor(final StringBuffer buf, final Class c) {
		Class d = c;
		while (true) {
			if (d.isPrimitive()) {
				char car;
				if (d == Integer.TYPE) {
					car = 'I';
				} else if (d == Void.TYPE) {
					car = 'V';
				} else if (d == Boolean.TYPE) {
					car = 'Z';
				} else if (d == Byte.TYPE) {
					car = 'B';
				} else if (d == Character.TYPE) {
					car = 'C';
				} else if (d == Short.TYPE) {
					car = 'S';
				} else if (d == Double.TYPE) {
					car = 'D';
				} else if (d == Float.TYPE) {
					car = 'F';
				} else /* if (d == Long.TYPE) */{
					car = 'J';
				}
				buf.append(car);
				return;
			} else if (d.isArray()) {
				buf.append('[');
				d = d.getComponentType();
			} else {
				buf.append('L');
				String name = d.getName();
				int len = name.length();
				for (int i = 0; i < len; ++i) {
					char car = name.charAt(i);
					buf.append(car == '.' ? '/' : car);
				}
				buf.append(';');
				return;
			}
		}
	}

}
