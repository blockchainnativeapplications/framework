package org.blockchainnative.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declares an argument of a contract method as {@code SpecialArgument}, i.e. an argument which has a special meaning
 *  to the underlying provider. How the argument is handled is up to the provider.
 *
 * @since 1.0
 * @author Matthias Veit
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface SpecialArgument {

    /** Specifies the name of the argument.
     *  Name and type of the argument need to match with one of the special arguments supported by the underlying provider.
     *
     * @return name of the argument
     */
    String value();
}
