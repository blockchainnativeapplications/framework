package org.blockchainnative.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By definition all parameters/arguments of event methods (i.e. methods annotated with {@link org.blockchainnative.annotations.ContractEvent})
 * are considered to be event parameters. These parameters are used by the underlying provider to create the {@link io.reactivex.Observable}
 * containing the events. <br>
 * <br>
 * {@code EventParameter} allows specifying the name of event parameters such that the underlying blockchain provider can identify them. <br>
 * How the argument is handled is up to the provider.
 *
 * @author Matthias Veit
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface EventParameter {

    /**
     * Specifies the name of the event parameter. <br>
     * Name and type of the parameter need to match with one of the parameters supported by the underlying provider.
     *
     * @return name of the event parameter
     */
    String value();
}
