/**
 * 
 */
package uk.co.jemos.podam.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.co.jemos.podam.annotations.PodamExclude;
import uk.co.jemos.podam.dto.ClassInfo;

/**
 * PODAM Utilities class.
 * 
 * @author mtedone
 * 
 * @since 1.0.0
 * 
 */

public class PodamUtils {

	// ---------------------->> Constants

	/** The application logger. */
	public static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(PodamUtils.class);

	/** Non instantiable constructor */
	private PodamUtils() {
		throw new AssertionError();
	}

	/**
	 * It returns a {@link ClassInfo} object for the given class
	 * 
	 * @param clazz
	 *            The class to retrieve info from
	 * @return a {@link ClassInfo} object for the given class
	 */
	public static ClassInfo getClassInfo(Class<?> clazz) {
        
		Set<String> classFields = getDeclaredInstanceFields(clazz);
        
		Map<String,Field> fieldToType = getRelevantFields(clazz);

		Set<Method> classSetters = getPojoSetters(clazz, classFields);

		return new ClassInfo(clazz, classFields, classSetters, fieldToType);
	}

	/**
	 * Given a class, it returns a Set of its declared instance field names.
	 * 
	 * @param clazz
	 *            The class to analyse to retrieve declared fields
     * @return Set of a class declared field names.
	 */
	public static Set<String> getDeclaredInstanceFields(Class<?> clazz) {
		Set<String> classFields = new HashSet<String>();

		while (clazz != null) {
			Field[] declaredFields = clazz.getDeclaredFields();
			for (Field field : declaredFields) {
				// If users wanted to skip this field, we grant their wishes
				if (field.getAnnotation(PodamExclude.class) != null) {
					continue;
				}
				int modifiers = field.getModifiers();
				if (!Modifier.isStatic(modifiers)) {

                    classFields.add(field.getName());
                }

			}
			clazz = clazz.getSuperclass();
		}

		return classFields;
	}


	/**
	 * Given a class, it returns a Map of its public field names, excluding
     * some -- such as those which are static.
	 * 
	 * @param clazz The class to analyse to retrieve declared fields
     * @return mapping from field names to Type of the field
	 */
	public static Map<String,Field> getRelevantFields(Class<?> clazz) {
        Map<String,Field> fieldToType = new HashMap<String,Field>();

		while (clazz != null) {
			Field[] declaredFields = clazz.getDeclaredFields();
			for (Field field : declaredFields) {
				// If users wanted to skip this field, we grant their wishes
				if (field.getAnnotation(PodamExclude.class) != null) {
					continue;
				}

                int modifiers = field.getModifiers();
				if (Modifier.isStatic(modifiers)) {
                    continue;
                }
				if (Modifier.isPrivate(modifiers)) {
                    continue;
                }
                /* By contract, I can just save this field object. */
                fieldToType.put(field.getName(), field);
			}
			clazz = clazz.getSuperclass();
		}

		return fieldToType;
	}

	/**
	 * Given a class and a set of class declared fields it returns a Set of
	 * setters matching the declared fields
	 * <p>
	 * If present, a setter method is considered if and only if the
	 * {@code classFields} argument contains an attribute whose name matches the
	 * setter, according to JavaBean standards.
	 * </p>
	 * 
	 * @param clazz
	 *            The class to analyse for setters
	 * @param classFields
	 *            A Set of field names for which setters are to be found
	 * @return A Set of setters matching the class declared field names
	 * 
	 */
	public static Set<Method> getPojoSetters(Class<?> clazz, Set<String> classFields) {

		Set<Method> classSetters = new HashSet<Method>();

		while (clazz != null) {

			Method[] declaredMethods = clazz.getDeclaredMethods();
			String candidateField;
			for (Method method : declaredMethods) {
				if (!method.getName().startsWith("set")) {
					continue;
				}
				if (!method.getReturnType().equals(void.class)) {
					continue;
				}
				candidateField = extractFieldNameFromSetterMethod(method);
				if (!classFields.contains(candidateField)) {
					continue;
				}
                
                // According to JavaBeans standards, setters should have only
                // one argument
                // check that setter has one argument
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    continue;
                }
                
                // TODO: check that one argument is the same type as the field
                
				classSetters.add(method);

			}
			clazz = clazz.getSuperclass();
		}

		return classSetters;
	}

	/**
	 * Given a setter {@link Method}, it extracts the field name, according to
	 * JavaBean standards
	 * <p>
	 * This method, given a setter method, it returns the corresponding
	 * attribute name. For example: given setIntField the method would return
	 * intField. The correctness of the return value depends on the adherence to
	 * JavaBean standards.
	 * </p>
	 * 
	 * @param method
	 *            The setter method from which the field name is required
	 * @return The field name corresponding to the setter
	 */
	public static String extractFieldNameFromSetterMethod(Method method) {
		String candidateField;
		candidateField = method.getName().substring(3);
		if (!candidateField.equals("")) {
			candidateField = Character.toLowerCase(candidateField.charAt(0))
					+ candidateField.substring(1);
		} else {
			LOG.warn("Encountered method set. This will be ignored.");
		}

		return candidateField;
	}

	/**
	 * Given a {@link Type} object, it extracts and returns the class name
	 * <p>
	 * If a {@link Type} was extracted from a {@link Field} object, it has the
	 * form: {@code class <classname>}, e.g. {@code class java.lang.String}.
	 * This method returns the {@code java.lang.String} part of the {@link Type}
	 * </p>
	 * 
	 * @param type
	 *            The type from which the class name must be extracted
	 * @return The class part of the Type format
	 */
	public static String extractClassNameFromParameterisedTypeInField(Type type) {
		String typeStr = type.toString();
		if (typeStr.startsWith("class")) {
			return typeStr.substring(6);
		} else if (typeStr.startsWith("interface")) {
			return typeStr.substring(10);
		} else if (typeStr.contains("<")) {
			int idx = typeStr.indexOf("<");
			return typeStr.substring(0, idx).trim();
		} else {
			throw new IllegalArgumentException("Type is not recognised");
		}

	}

}
