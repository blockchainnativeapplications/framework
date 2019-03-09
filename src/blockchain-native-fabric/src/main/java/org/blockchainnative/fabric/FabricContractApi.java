package org.blockchainnative.fabric;

import io.reactivex.Observable;
import org.blockchainnative.fabric.metadata.FabricContractInfo;
import org.blockchainnative.metadata.Event;
import org.blockchainnative.metadata.Result;
import org.hyperledger.fabric.sdk.User;

import java.util.Collection;

/**
 * Wraps the calls to the Hyperledger Fabric SDK for a given contract.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public interface FabricContractApi {

    /**
     * Creates an observable for a given event.
     *
     * @param eventName name of the Hyperledger Fabric chaincode event
     * @return raw event observable
     */
    Observable<Event<String>> createChaincodeEventObservable(String eventName);

    /**
     * Installs a the chaincode on given peers.
     *
     * @param targetPeerNames peers to install the chaincode on. <br> If null, the peers registered in {@link
     *                        FabricContractInfo} are used. If those are set as well, the chaincode is installed on all
     *                        peers known to the channel
     * @param user            user context to be used. If null the clients user context is used. <br> NOTE: Make sure
     *                        that the user has correct permissions to install chaincode on the target peers.
     */
    void installChaincode(Collection<String> targetPeerNames, User user);

    /**
     * Instantiates the chaincode. <br> Make sure to install the chaincode on all target peers before calling
     * instantiate.
     *
     * @param arguments       chaincode init arguments
     * @param targetPeerNames peers to instantiate the chaincode on. <br> If null, the peers registered in {@link
     *                        FabricContractInfo} are used. If those are set as well, the chaincode is installed on all
     *                        peers known to the channel
     * @param user            user context to be used. If null the clients user context is used. <br> NOTE: Make sure
     *                        that the user has correct permissions to submit an instantiate transaction to the
     *                        orderer.
     */
    void instantiateChaincode(String[] arguments, Collection<String> targetPeerNames, User user);

    /**
     * Executes a chaincode function in a transaction.
     *
     * @param functionName    name of the function to be executed
     * @param arguments       function arguments
     * @param targetPeerNames peers to target with the function call. <br> If null, the peers registered in {@link
     *                        FabricContractInfo} are used. If those are set as well, the chaincode is installed on all
     *                        peers known to the channel
     * @param user            user context to be used. If null the clients user context is used.
     * @return raw function result
     */
    Result<String> callChaincode(String functionName, String[] arguments, Collection<String> targetPeerNames, User user);

    /**
     * Executes a chaincode function without submitting a transaction.
     *
     * @param functionName    name of the function to be executed
     * @param arguments       function arguments
     * @param targetPeerNames peers to target with the function call. <br> If null, the peers registered in {@link
     *                        FabricContractInfo} are used. If those are set as well, the chaincode is installed on all
     *                        peers known to the channel
     * @param user            user context to be used. If null the clients user context is used.
     * @return raw function result
     */
    Result<String> queryChaincode(String functionName, String[] arguments, Collection<String> targetPeerNames, User user);

    /**
     * Sets the {@code FabricContractInfo} the {@code FabricContractApi} operates on
     *
     * @param contractInfo {@code FabricContractInfo}
     */
    void setContractInfo(FabricContractInfo<?> contractInfo);
}
