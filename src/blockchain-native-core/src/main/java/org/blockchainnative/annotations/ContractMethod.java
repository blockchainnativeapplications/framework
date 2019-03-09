package org.blockchainnative.annotations;

import org.blockchainnative.convert.NoOpTypeConverter;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.convert.TypeConverters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a method to be mapped the corresponding method of a smart contract. <br>
 * Contract methods can use the following return types:
 * <ul>
 *      <li>Any type supported by the underlying provider</li>
 *      <li>Any type if an appropriate type converter is registered</li>
 *      <li>Any of the above wrapped in type {@link org.blockchainnative.metadata.Result}</li>
 *      <li>Any of the abover wrapped int {@link java.util.concurrent.Future} or {@link java.util.concurrent.CompletableFuture}</li>
 * </ul>
 *
 * @author Matthias Veit
 * @see org.blockchainnative.ContractWrapperGenerator
 * @see org.blockchainnative.metadata.Result
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ContractMethod {

    /**
     * Explicitly specifies the name of the method to be called in the associated smart contract. <br>
     * If no name is specified (i.e. an empty string) the name of the method to which this annotation is applied is used.
     *
     * @return name of the method to be called in the associated smart contract.
     */
    String value() default "";

    /**
     * Declares whether or not the smart contracts method is meant to be invoked in a way so that it is modifying the blockchain's state. <br>
     * If set to {@code true}, the blockchain provider is instructed to call the method without modifying the blockchain as long the system supports non-modifying calls. <br>
     * If such behavior is not supported, an  {@link java.lang.UnsupportedOperationException} is to raised when invoking the method.
     *
     * @return {@code boolean} indicating whether this method is declared as readonly or not
     */
    boolean isReadOnly() default false;

    /**
     * Marks the method to have a special meaning to the underlying provider. <br>
     * How this method is interpreted is up to provider.
     *
     * @return {@code boolean} indicating whether this method is declared as special method or not
     */
    boolean isSpecialMethod() default false;

    /**
     * Explicitly specifies a type converter to convert the smart contract's result object to the methods declared return type. <br>
     * If no converter (i.e. {@link NoOpTypeConverter}) is declared, the provider will try to find a matching converter
     * registered with {@link TypeConverters} or attempt to convert the type with its built-in mechanism (if supported).
     *
     * @return specific type converter class to be used.
     */
    Class<? extends TypeConverter<?, ?>> useTypeConverter() default NoOpTypeConverter.class;
}
