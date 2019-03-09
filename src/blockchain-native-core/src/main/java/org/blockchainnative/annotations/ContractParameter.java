package org.blockchainnative.annotations;

import org.blockchainnative.convert.NoOpTypeConverter;
import org.blockchainnative.convert.TypeConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds additional metadata to the parameters of a method annotated with {@link org.blockchainnative.annotations.ContractMethod}. <br>
 * Either {@code asType()} or {@code useTypeConverter()}, but not both fields are intended to be set.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.ContractWrapperGenerator
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ContractParameter {

    /**
     * Instructs to underlying provider to convert the annotated parameter to the following type before calling the contract method. <br>
     * The provider will use any matching type converter or its built-in conversion mechanism (if supported) the convert the parameter. <br>
     * If the type is not supported by the provider, a {@link org.blockchainnative.exceptions.TypeConvertException} is raised when calling the method. <br>
     * {@code Void.class} acts as a placeholder, meaning no special conversion is needed. <br>
     * <br>
     * Due to type erasure you need to use a specific type converter in order to convert to a generic type.
     *
     * @return type to convert the parameter to
     */
    Class<?> asType() default Void.class;

    /**
     * Instructs to underlying provider to convert the annotated parameter using the specified type converter before calling the contract method. <br>
     * If the resulting type is not supported by the provider, a {@link org.blockchainnative.exceptions.TypeConvertException} is raised when calling the method. <br>
     * {@code NoOpTypeConverter.class} acts as a placeholder, meaning no special conversion is needed.
     *
     * @return specific type converter class to be used.
     */
    Class<? extends TypeConverter<?, ?>> useTypeConverter() default NoOpTypeConverter.class;
}
