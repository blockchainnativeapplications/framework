package org.blockchainnative.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO use this annotation to scan for all contracts at application startup and register them for DI through the use of the contract registry

/**
 * Marks an interface as representation of a smart contract. <br>
 * The usage of this annotation is optional.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.builder.ContractInfoBuilder
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SmartContract {

    /**
     * Explicitly specifies the identifier smart contract. <br>
     * The identifier is used to differentiate between multiple instances of the same contract interface,
     * therefore specifying it via this annotation only makes sense for contracts only a single instance. <br>
     * If no identifier is specified (i.e. an empty string), the identifier can be specified via the {@link org.blockchainnative.builder.ContractInfoBuilder},
     * or otherwise a random identifier will be generated.
     *
     * @return identifier of the smart contract
     */
    String value() default "";
}
