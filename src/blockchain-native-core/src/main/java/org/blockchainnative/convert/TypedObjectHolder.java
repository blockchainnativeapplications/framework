package org.blockchainnative.convert;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Type;

/**
 * Contains an object's value together with its {@code Type} in order to work around Java's implementation of Generics and the resulting type erasure.
 *
 * @author Matthias Veit
 * @see TypeConverter
 * @see TypeConverters
 * @since 1.0
 */
public class TypedObjectHolder {
    private final Type type;
    private final Object object;

    /**
     * Creates a new instance of {@code TypedObjectHolder}
     *
     * @param type   declared type of the given object
     * @param object object to be whose type information needs to be preserved
     */
    public TypedObjectHolder(Type type, Object object) {
        this.type = type;
        this.object = object;
    }

    /**
     * Returns the declared type of the object contained in this {@code TypedObjectHolder}
     *
     * @return declared type of the object contained in this {@code TypedObjectHolder}
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the object contained in this {@code TypedObjectHolder}
     *
     * @return object contained in this {@code TypedObjectHolder}
     */
    public Object getObject() {
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypedObjectHolder)) return false;

        TypedObjectHolder that = (TypedObjectHolder) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return object != null ? object.equals(that.object) : that.object == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (object != null ? object.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("object", object)
                .toString();
    }
}
