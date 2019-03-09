package org.blockchainnative.ethereum.test.contracts;

import io.reactivex.Observable;
import org.blockchainnative.annotations.*;
import org.blockchainnative.ethereum.Constants;
import org.blockchainnative.metadata.Event;
import org.blockchainnative.metadata.Result;
import org.blockchainnative.test.contracts.HelloContract;
import org.blockchainnative.test.contracts.HelloContractWithBlockInformation;

import java.math.BigInteger;

/**
 * @author Matthias Veit
 */
@SmartContract
public interface EthereumHelloContractWithBlockInformation extends HelloContractWithBlockInformation {

    @ContractMethod(isSpecialMethod = true)
    Result<String> deploy(@SpecialArgument(Constants.GAS_PRICE_ARGUMENT) BigInteger gasPrice,
                  @SpecialArgument(Constants.GAS_LIMIT_ARGUMENT) BigInteger gasLimit,
                  String greeting);

    @ContractEvent("greeted")
    Observable<Event<EthereumHelloContractWithEvent.HelloEvent>> onGreeted();

    class HelloEvent {

        @EventField("name")
        public String name;
    }
}
