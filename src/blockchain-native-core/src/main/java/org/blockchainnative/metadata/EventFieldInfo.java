package org.blockchainnative.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.convert.TypeConverter;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Holds additional information about a field of a class which is used as smart contract event type. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.builder.ContractInfoBuilder} and its subtypes to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.builder.ContractInfoBuilder
 * @see ContractInfo
 * @see org.blockchainnative.ContractWrapperGenerator
 * @since 1.0
 */
public class EventFieldInfo {
    protected final Field field;
    protected final String sourceFieldName;
    protected final Optional<Integer> sourceFieldIndex;
    protected final Optional<Class<? extends TypeConverter<?, ?>>> typeConverterClass;

    /**
     * Constructs a new {@code EventFieldInfo}.
     *
     * @param field              field targeted by this {@code EventFieldInfo}
     * @param sourceFieldName    name of corresponding field in the smart contract event
     * @param sourceFieldIndex   index of corresponding field in the smart contract event
     * @param typeConverterClass type converter used to convert the smart contract event field value to the declared type of this field
     */
    public EventFieldInfo(Field field, String sourceFieldName, Optional<Integer> sourceFieldIndex, Optional<Class<? extends TypeConverter<?, ?>>> typeConverterClass) {
        this.field = field;
        this.sourceFieldName = sourceFieldName;
        this.sourceFieldIndex = sourceFieldIndex;
        this.typeConverterClass = typeConverterClass;
    }

    /**
     * Returns the field of the corresponding event type.
     *
     * @return field of the corresponding event type.
     */
    public Field getField() {
        return field;
    }

    /**
     * Gets the {@code TypeConverter} to be used to convert the smart contract event field value to the declared type of the field on the event type.
     *
     * @return {@code TypeConverter} to be used to convert the smart contract event field value.
     */
    public Optional<Class<? extends TypeConverter<?, ?>>> getTypeConverterClass() {
        return typeConverterClass;
    }

    /**
     * Returns the name of the field as it is called in corresponding smart contract event. <br>
     * If the blockchain's implementation of events does not support multiple or named fields, this value is meaningless.
     *
     * @return name of the field as it is called in corresponding smart contract event.
     */
    public String getSourceFieldName() {
        return sourceFieldName;
    }

    /**
     * Returns the index of the targeted field of the corresponding smart contract event. <br>
     * If the blockchain's implementation of events does not support multiple fields, this value is meaningless.
     *
     * @return index of the targeted field of the corresponding smart contract event.
     */
    public Optional<Integer> getSourceFieldIndex() {
        return sourceFieldIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof EventFieldInfo)) return false;

        EventFieldInfo that = (EventFieldInfo) o;

        return new EqualsBuilder()
                .append(sourceFieldIndex, that.sourceFieldIndex)
                .append(field, that.field)
                .append(sourceFieldName, that.sourceFieldName)
                .append(typeConverterClass, that.typeConverterClass)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(field)
                .append(sourceFieldName)
                .append(sourceFieldIndex)
                .append(typeConverterClass)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("field", field)
                .append("sourceFieldName", sourceFieldName)
                .append("index", sourceFieldIndex)
                .toString();
    }
}
