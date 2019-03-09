package org.blockchainnative.fabric.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the language of a Hyperledger Fabric chaincode.
 *
 * @since 1.0
 * @author Matthias Veit
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ChaincodeLanguage {

    /**
     * Language of the Hyperledger Fabric chaincode. <br>
     * Needs to be specified in order to be able to deploy the chaincode.
     *
     * @return Language of the Hyperledger Fabric chaincode
     */
    org.blockchainnative.fabric.metadata.ChaincodeLanguage value() default org.blockchainnative.fabric.metadata.ChaincodeLanguage.Undefined;
}

