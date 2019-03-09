package org.blockchainnative.ethereum.test.contracts;

import io.reactivex.Observable;
import org.blockchainnative.annotations.ContractEvent;
import org.blockchainnative.annotations.EventField;
import org.blockchainnative.annotations.SmartContract;

/**
 * @author Matthias Veit
 */
@SmartContract
public interface EthereumHelloContractWithEvent extends EthereumHelloContract {

    @ContractEvent("greeted")
    Observable<HelloEvent> onGreeted();

    class HelloEvent {

        @EventField("name")
        public String name;
    }
}
