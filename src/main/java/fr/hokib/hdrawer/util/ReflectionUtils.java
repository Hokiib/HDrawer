package fr.hokib.hdrawer.util;

public class ReflectionUtils {
	
	/**
	 * Call static method. Return the method return.
	 * 
	 * @param source the object where we want to run the method
	 * @param method the name of the method to call
	 * @return the return of the method called
	 */
	public static Object callStaticMethod(Class<?> source, String method) {
		try {
			return source.getDeclaredMethod(method).invoke(null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isClassExist(String clazz) {
		try {
			Class.forName(clazz);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
