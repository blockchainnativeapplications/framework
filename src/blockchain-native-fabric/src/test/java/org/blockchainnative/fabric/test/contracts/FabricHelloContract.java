package org.blockchainnative.fabric.test.contracts;

import io.reactivex.Observable;
import org.blockchainnative.annotations.*;
import org.blockchainnative.fabric.Constants;
import org.blockchainnative.test.contracts.HelloContract;
import org.hyperledger.fabric.sdk.User;

import java.util.Set;

/**
 * @author Matthias Veit
 */
@SmartContract
public interface FabricHelloContract extends HelloContract {

    @ContractMethod(isSpecialMethod = true)
    void install(@SpecialArgument(Constants.TARGET_PEERS_ARGUMENT) Set<String> targetPeers,
                 @SpecialArgument(Constants.USER_ARGUMENT) User user);

    @ContractMethod(isSpecialMethod = true)
    void instantiate(String greeting,
                     @SpecialArgument(Constants.TARGET_PEERS_ARGUMENT) Set<String> targetPeers,
                     @SpecialArgument(Constants.USER_ARGUMENT) User user);

    @ContractEvent("greeted")
    Observable<HelloEvent> onGreeted();

    class HelloEvent {

        @EventField("name")
        public String name;
    }
}
