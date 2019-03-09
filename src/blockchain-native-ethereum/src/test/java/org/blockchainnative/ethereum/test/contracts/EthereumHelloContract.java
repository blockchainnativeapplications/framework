package org.blockchainnative.ethereum.test.contracts;

import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.annotations.SmartContract;
import org.blockchainnative.annotations.SpecialArgument;
import org.blockchainnative.ethereum.Constants;
import org.blockchainnative.test.contracts.HelloContract;

import java.math.BigInteger;
import java.util.concurrent.Future;

/**
 * @author Matthias Veit
 */
@SmartContract
public interface EthereumHelloContract extends HelloContract {

    @ContractMethod(isSpecialMethod = true)
    void deploy(@SpecialArgument(Constants.GAS_PRICE_ARGUMENT) BigInteger gasPrice,
                @SpecialArgument(Constants.GAS_LIMIT_ARGUMENT) BigInteger gasLimit,
                String greeting);
}
