package org.blockchainnative.annotations;

import org.blockchainnative.convert.NoOpTypeConverter;
import org.blockchainnative.convert.TypeConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds additional metadata to the fields of a class that is used as event type. <br>
 * <br>
 * Event fields represent the fields of classes used as the result type of methods annotated with {@link ContractEvent}. <br>
 * Each field is meant to be mapped to a corresponding field of an event declared by a smart contract. <br>
 * Depending on the underlying blockchain, events are defined differently (or not supported at all). <br>
 * If an event contains multiple values, these values are mapped to the event object either by name or index. <br>
 * <br>
 * Either {@code value()} or {@code index()}, but not both fields are intended to be set. <br>
 *
 * @author Matthias Veit
 * @see org.blockchainnative.ContractWrapperGenerator
 * @see org.blockchainnative.annotations.ContractEvent
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EventField {

    /**
     * Explicitly specifies the name of field as defined in the event of the associated smart contract. <br>
     * If no name is specified (i.e. an empty string) the name of the field to which this annotation is applied is used.
     *
     * @return name of the field in the associated smart contract event
     */
    String value() default "";

    /**
     * Specifies the index of field as defined in the event of the associated smart contract. <br>
     * If index is set, the underlying provider will use it instead of the name to identify the corresponding field. <br>
     * A value of -1 acts as a placeholder and has no effect.
     *
     * @return index of the field in the associated smart contract event
     */
    int index() default -1;

    /**
     * Instructs to underlying provider to convert the corresponding smart contract event field value to the declared type of the field in the event object using the specified type converter. <br>
     * If the resulting type does not match the field's declared type, a {@link org.blockchainnative.exceptions.TypeConvertException} is raised. <br>
     * {@code NoOpTypeConverter.class} acts as a placeholder, meaning no special conversion is needed.
     *
     * @return type converter used to convert the field value.
     */
    Class<? extends TypeConverter<?, ?>> useTypeConverter() default NoOpTypeConverter.class;
}
