package org.blockchainnative.fabric.test.contracts;

import io.reactivex.Observable;
import org.blockchainnative.annotations.*;
import org.blockchainnative.fabric.Constants;
import org.blockchainnative.metadata.Event;
import org.blockchainnative.metadata.Result;
import org.blockchainnative.test.contracts.HelloContract;
import org.hyperledger.fabric.sdk.User;

import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author Matthias Veit
 */
@SmartContract
public interface FabricHelloContractWithEventAndResult {

    @ContractMethod
    Result<String> hello(String name);

    @ContractMethod("hello")
    Future<Result<String>> helloAsync(String name);

    @ContractMethod(value = "hello", isReadOnly = true)
    Future<Result<String>> helloReadOnly(String name);

    @ContractMethod(isSpecialMethod = true)
    void install(@SpecialArgument(Constants.TARGET_PEERS_ARGUMENT) Set<String> targetPeers,
                 @SpecialArgument(Constants.USER_ARGUMENT) User user);

    @ContractMethod(isSpecialMethod = true)
    void instantiate(String greeting,
                     @SpecialArgument(Constants.TARGET_PEERS_ARGUMENT) Set<String> targetPeers,
                     @SpecialArgument(Constants.USER_ARGUMENT) User user);

    @ContractEvent("greeted")
    Observable<Event<HelloEvent>> onGreeted();

    class HelloEvent {

        @EventField("name")
        public String name;
    }
}
