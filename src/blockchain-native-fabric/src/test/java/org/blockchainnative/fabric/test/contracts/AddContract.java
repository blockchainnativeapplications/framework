package org.blockchainnative.fabric.test.contracts;

import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.annotations.SmartContract;
import org.blockchainnative.annotations.SpecialArgument;
import org.blockchainnative.fabric.Constants;
import org.hyperledger.fabric.sdk.User;

import java.util.Set;

/**
 * @author Matthias Veit
 */
@SmartContract
public interface AddContract {

    @ContractMethod(isSpecialMethod = true)
    void install(@SpecialArgument(Constants.TARGET_PEERS_ARGUMENT) Set<String> targetPeers,
                 @SpecialArgument(Constants.USER_ARGUMENT) User user);

    @ContractMethod(isSpecialMethod = true)
    void instantiate(@SpecialArgument(Constants.TARGET_PEERS_ARGUMENT) Set<String> targetPeers,
                     @SpecialArgument(Constants.USER_ARGUMENT) User user);

    @ContractMethod("add")
    int addInt(int x, int y);
}
