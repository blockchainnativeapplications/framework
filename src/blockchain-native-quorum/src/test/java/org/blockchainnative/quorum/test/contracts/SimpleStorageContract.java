package org.blockchainnative.quorum.test.contracts;

import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.annotations.SmartContract;
import org.blockchainnative.annotations.SpecialArgument;
import org.blockchainnative.quorum.Constants;
import org.blockchainnative.quorum.QuorumSmartContract;

import java.util.List;

/**
 * @author Matthias Veit
 */
@SmartContract
public interface SimpleStorageContract extends QuorumSmartContract<SimpleStorageContract> {

    @ContractMethod(isSpecialMethod = true)
    void deploy();

    @ContractMethod(isReadOnly = true)
    int get();

    @ContractMethod
    void set(int x, @SpecialArgument(Constants.PRIVATE_FOR_ARGUMENT) List<String> privateFor);
}
