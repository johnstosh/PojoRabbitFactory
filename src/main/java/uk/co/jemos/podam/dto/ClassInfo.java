/**
 *
 */
package uk.co.jemos.podam.dto;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.Immutable;

/**
 * This class wraps fields and setters information about a given class <p> The purpose of this class
 * is to work as a sort of cache which stores the list of declared fields and setter methods of a
 * given class. These information will then be analysed to compose the list of setters which can be
 * invoked to create the state of a given POJO. </p>
 *
 * @author mtedone
 *
 * @since 1.0.0
 *
 */
@Immutable
public class ClassInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * The Class name whose info are stored in this class
     */
    private final Class<?> className;
    /**
     * The Set of fields belonging to this class
     */
    private final Set<String> classFields;
    /**
     * The Set of setters belonging to this class
     */
    private final Set<Method> classSetters;
    /**
     * A Mapping from a subset of fields that are relevant, to the Field
     * objects (ala reflection) for those fields.
     */
    private Map<String, Field> fieldToType;

    /**
     * Full constructor
     *
     * @param className The class name
     * @param classFields The set of fields belonging to this class
     * @param classSetters The set of setters belonging to this class
     * @param fieldToType mapping from field names to Type of the field
     */
    public ClassInfo(Class<?> className, Set<String> classFields,
            Set<Method> classSetters, Map<String, Field> fieldToType) {
        super();
        this.className = className;
        this.classFields = classFields;
        this.classSetters = classSetters;
        this.fieldToType = fieldToType;
    }

    /**
     * The set of POJO setters, following certain rules, that were obtained
     * (by reflection) and saved into this ClassInfo object during its
     * instantiation.  The set is usually obtained by calling the 
     * PodamUtils.getPojoSetters() method.
     * 
     * @return the classSetters
     * @see PodamUtils.getPojoSetters()
     */
    public Set<Method> getClassSetters() {
        return classSetters;
    }

    /**
     * The set of fields obtained (by reflection) from the class.
     * 
     * @return 
     */
    public Set<String> getClassFields() {
        return classFields;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.className != null ? this.className.hashCode() : 0);
        hash = 89 * hash + (this.classFields != null ? this.classFields.hashCode() : 0);
        hash = 89 * hash + (this.classSetters != null ? this.classSetters.hashCode() : 0);
        hash = 89 * hash + (this.fieldToType != null ? this.fieldToType.hashCode() : 0);
        return hash;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClassInfo other = (ClassInfo) obj;
        if (this.className != other.className && (this.className == null || !this.className.equals(other.className))) {
            return false;
        }
        if (this.classFields != other.classFields && (this.classFields == null || !this.classFields.equals(other.classFields))) {
            return false;
        }
        if (this.classSetters != other.classSetters && (this.classSetters == null || !this.classSetters.equals(other.classSetters))) {
            return false;
        }
        if (this.fieldToType != other.fieldToType && (this.fieldToType == null || !this.fieldToType.equals(other.fieldToType))) {
            return false;
        }
        return true;
    }

    /**
     * Constructs a
     * <code>String</code> with all attributes in name = value format.
     *
     * @return a
     * <code>String</code> representation of this object.
     */
    @Override
    public String toString() {
        final String TAB = "    ";

        StringBuilder retValue = new StringBuilder();

        retValue.append("ClassInfo ( ").append(TAB).append("className = ").append(className).append(TAB).append("classFields = ").append(classFields).append(TAB).append("classSetters = ").append(classSetters).append(TAB).append(" )");

        return retValue.toString();
    }

    /**
     * Get the Field object for a relevant field name of the class, using the
     * saved fieldToType mapping from when this ClassInfo object was
     * constructed. Note that the fields in this mapping are a subset of the
     * total list of fields, so you'll have to check for null.
     *
     * @param name as perhaps obtained from the getClassFields() call
     * @return data obtained earlier from reflection, or perhaps null if the
     * field isn't relevant.
     */
    public Field getField(String name) {
        return fieldToType.get(name);
    }
}
