package org.blockchainnative.ethereum.test.contracts;

import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.annotations.SmartContract;
import org.blockchainnative.test.contracts.AdditionContract;

/**
 * @author Matthias Veit
 */
@SmartContract
public interface EthereumAdditionContract extends AdditionContract {

    @ContractMethod(value = "deploy", isSpecialMethod = true)
    void deploy();
}
