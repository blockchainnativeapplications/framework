package org.blockchainnative.ethereum.test.contracts;

import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.annotations.ContractParameter;

import java.util.List;
import java.util.UUID;

public interface TypeConverterTestContract {

    @ContractMethod
    void uuidParameter(UUID uuid);

    @ContractMethod()
    UUID uuidReturnType();

    @ContractMethod("uuidParameter")
    void uuidParameterPassAsType(@ContractParameter(asType = String.class) UUID uuid);

    @ContractMethod
    byte byteReturnType();

    @ContractMethod
    void listParameter(List<UUID> uuids);

    @ContractMethod
    List<UUID> uuidListReturnType();

    @ContractMethod
    void twoDimensionalListParameter(List<List<UUID>> uuids);

    @ContractMethod
    void arrayParameter(short[] shortArray);

    @ContractMethod
    void twoDimensionalArrayParameter(short[][] shortArrayArray);
}
