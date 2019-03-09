package org.blockchainnative.fabric.test.contracts;

import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.annotations.ContractParameter;

import java.util.List;
import java.util.UUID;

public interface TypeConverterTestContract {

    @ContractMethod
    void uuidParameter(UUID uuid);

    @ContractMethod("uuidParameter")
    void uuidParameterPassAsType(@ContractParameter(asType = String.class) UUID uuid);

    @ContractMethod
    UUID uuidReturnType();

    @ContractMethod
    byte byteReturnType();
}
