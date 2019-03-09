package org.blockchainnative.fabric.test.contracts;

import io.reactivex.Observable;
import org.blockchainnative.annotations.*;
import org.blockchainnative.fabric.Constants;
import org.blockchainnative.test.contracts.HelloContract;
import org.blockchainnative.test.contracts.HelloContractWithoutAnnotations;
import org.hyperledger.fabric.sdk.User;

import java.util.Set;

/**
 * @author Matthias Veit
 */
public interface FabricHelloContractWithoutAnnotations extends HelloContractWithoutAnnotations {

    void install(Set<String> targetPeers, User user);

    void instantiate(String greeting, Set<String> targetPeers, User user);

    Observable<HelloEvent> onGreeted();

    class HelloEvent {

        public String name;
    }
}
