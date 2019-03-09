package org.blockchainnative.fabric;

import io.reactivex.Observable;
import org.blockchainnative.AbstractContractWrapper;
import org.blockchainnative.SpecialMethodDelegate;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.fabric.metadata.*;
import org.blockchainnative.metadata.Event;
import org.blockchainnative.metadata.Result;
import org.blockchainnative.util.ReflectionUtil;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Base wrapper class for Ethereum smart contracts used by {@link FabricContractWrapperGenerator}
 *
 * @author Matthias Veit
 * @see FabricContractWrapperGenerator
 * @since 1.0
 */
public class FabricContractWrapper extends AbstractContractWrapper<FabricContractInfo<?>, FabricMethodInfo, FabricParameterInfo, FabricEventInfo, FabricEventFieldInfo, FabricEventParameterInfo> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricContractWrapper.class);

    private final FabricContractApi contractApi;
    private final FabricArgumentConverter argumentConverter;

    public FabricContractWrapper(FabricContractInfo<?> contractInfo, HFClient client, Channel channel, TypeConverters typeConverters) {
        super(contractInfo);
        this.contractApi = new FabricContractApiImpl(client, channel, contractInfo);
        this.argumentConverter = new FabricArgumentConverterImpl(typeConverters);
    }

    public FabricContractWrapper(FabricContractInfo<?> contractInfo, FabricContractApi contractApi, FabricArgumentConverter argumentConverter) {
        super(contractInfo);
        this.contractApi = contractApi;
        this.contractApi.setContractInfo(contractInfo);
        this.argumentConverter = argumentConverter;
    }

    /**
     * Returns a mapping of special method names to their actions. <br> {@code FabricContractWrapper} registers two
     * mappings, {@link Constants#INSTALL_METHOD} to {@link FabricContractWrapper#install(FabricMethodInfo, Object[])}
     * and {@link Constants#INSTANTIATE_METHOD} to {@link FabricContractWrapper#instantiate(FabricMethodInfo,
     * Object[])}
     *
     * @return mapping of special method names to their actions.
     */
    @Override
    protected Map<String, SpecialMethodDelegate<FabricMethodInfo>> getSpecialMethods() {
        return new HashMap<>() {{
            put(Constants.INSTALL_METHOD, ((methodInfo, args) -> install(methodInfo, args)));
            put(Constants.INSTANTIATE_METHOD, ((methodInfo, args) -> instantiate(methodInfo, args)));
        }};
    }

    /**
     * Creates an event observable for the given event method
     *
     * @param eventInfo {@code FabricEventInfo} describing the corresponding chaincode event.
     * @param arguments arguments of the smart contract interface method
     * @return observable containing the events emitted by the chaincode converted to the expected type
     */
    @Override
    protected Observable<Object> createEventObservable(FabricEventInfo eventInfo, Object[] arguments) {
        LOGGER.info("Preparing event observable '{}'", eventInfo.getEventName());

        var chaincodeEventObservable = contractApi.createChaincodeEventObservable(eventInfo.getEventName());

        return chaincodeEventObservable.map(event -> {

            var payload = event.getData();
            var instance = this.argumentConverter.createEventObject(eventInfo, payload);

            if (ReflectionUtil.usesEventWrapper(eventInfo.getMethod())) {
                return new Event<>(instance, event.getBlockHash(), event.getTransactionHash());
            } else {
                return instance;
            }
        });
    }

    private Future<Void> install(FabricMethodInfo methodInfo, Object[] arguments) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Preparing to install chaincode '{}'", this.contractInfo.getChaincodeID());

            var targetPeerNames = getTargetPeerNames(methodInfo, arguments);
            var user = extractUser(methodInfo, arguments);

            contractApi.installChaincode(targetPeerNames, user);

            return null;
        });
    }

    private Future<Void> instantiate(FabricMethodInfo methodInfo, Object[] arguments) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Preparing to instantiate chaincode '{}'", this.contractInfo.getChaincodeID());

            var targetPeers = getTargetPeerNames(methodInfo, arguments);
            var user = extractUser(methodInfo, arguments);

            var deploymentArguments = extractContractMethodParameters(methodInfo, arguments);
            var parameterInfos = extractContractMethodParameterInfos(methodInfo);
            var convertedArguments = this.argumentConverter.convertArguments(parameterInfos, deploymentArguments).toArray(new String[0]);

            contractApi.instantiateChaincode(convertedArguments, targetPeers, user);

            return null;
        });
    }

    /**
     * Invokes the given smart contract method while submitting a transaction to the Hyperledger Fabric blockchain.
     *
     * @param methodInfo {@code FabricMethodInfo} describing the corresponding smart contract method.
     * @param arguments  arguments of the smart contract interface method
     * @return actual smart contract method result converted to the expected type and wrapped as {@link
     *         java.util.concurrent.Future}
     */
    protected Future<Object> invokeReadOnlyMethod(FabricMethodInfo methodInfo, Object[] arguments) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Preparing readonly function call '{}.{}()'", this.contractInfo.getChaincodeID().getName(), methodInfo.getContractMethodName());

            var targetPeers = getTargetPeerNames(methodInfo, arguments);
            var user = extractUser(methodInfo, arguments);

            LOGGER.debug("Converting arguments...");

            var methodArguments = extractContractMethodParameters(methodInfo, arguments);
            var parameterInfos = extractContractMethodParameterInfos(methodInfo);
            var convertedArgs = this.argumentConverter.convertArguments(parameterInfos, methodArguments);

            LOGGER.debug("Arguments: {}", convertedArgs.stream().map(x -> String.format("'%s'", x)).collect(Collectors.joining(", ")));

            var stringResult = this.contractApi.queryChaincode(methodInfo.getContractMethodName(), convertedArgs.toArray(new String[0]), targetPeers, user);

            LOGGER.debug("Converting result to target type... Result: '{}'", stringResult);

            var convertedResult = this.argumentConverter.convertMethodResult(methodInfo, stringResult.getData());

            if (ReflectionUtil.usesResultWrapper(methodInfo.getMethod())) {
                return new Result<>(convertedResult, null, null);
            } else {
                return convertedResult;
            }
        });
    }

    /**
     * Invokes the given smart contract method without creating a transaction on the Hyperledger Fabric blockchain.
     *
     * @param methodInfo {@code FabricMethodInfo} describing the corresponding smart contract method.
     * @param arguments  arguments of the smart contract interface method
     * @return actual smart contract method result converted to the expected type and wrapped as {@link
     *         java.util.concurrent.Future}
     */
    protected Future<Object> invokeMethod(FabricMethodInfo methodInfo, Object[] arguments) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Preparing function call '{}.{}()'", this.contractInfo.getChaincodeID().getName(), methodInfo.getContractMethodName());

            var targetPeers = getTargetPeerNames(methodInfo, arguments);
            var user = extractUser(methodInfo, arguments);

            LOGGER.debug("Converting arguments...");

            var methodArguments = extractContractMethodParameters(methodInfo, arguments);
            var parameterInfos = extractContractMethodParameterInfos(methodInfo);
            var convertedArgs = this.argumentConverter.convertArguments(parameterInfos, methodArguments);

            LOGGER.debug("Arguments: {}", convertedArgs.stream().map(x -> String.format("'%s'", x)).collect(Collectors.joining(", ")));

            var stringResult = this.contractApi.callChaincode(methodInfo.getContractMethodName(), convertedArgs.toArray(new String[0]), targetPeers, user);

            LOGGER.debug("Converting result to target type... Result: '{}'", stringResult);

            var convertedResult = this.argumentConverter.convertMethodResult(methodInfo, stringResult.getData());

            if (ReflectionUtil.usesResultWrapper(methodInfo.getMethod())) {
                return new Result<>(convertedResult, stringResult.getBlockHash(), stringResult.getTransactionHash());
            } else {
                return convertedResult;
            }
        });
    }

    private Collection<String> extractTargetPeers(FabricMethodInfo methodInfo, Object[] arguments) {
        var targetPeers = extractSpecialArgument(methodInfo, Constants.TARGET_PEERS_ARGUMENT, arguments);
        if (targetPeers == null) {
            return null;
        }

        if (targetPeers instanceof Collection) {
            return ((Collection<?>) targetPeers).stream().map(Object::toString).collect(Collectors.toList());
        } else {
            var message = String.format("Unexpected type of additional field '%s'. Given: '%s', expected: java.util.Collection<String>.", Constants.TARGET_PEERS_ARGUMENT, targetPeers.getClass().getName());
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    private User extractUser(FabricMethodInfo methodInfo, Object[] arguments) {
        var user = extractSpecialArgument(methodInfo, Constants.USER_ARGUMENT, arguments);
        if (user == null) {
            return null;
        }
        if (user instanceof User) {
            return (User) user;
        } else {
            var message = String.format("Unexpected type of additional field '%s'. Given: '%s', expected: '%s'.", Constants.USER_ARGUMENT, user.getClass().getName(), User.class.getName());
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    private Collection<String> getTargetPeerNames(FabricMethodInfo methodInfo, Object[] arguments) {
        var methodTargetPeerNames = extractTargetPeers(methodInfo, arguments);
        if (methodTargetPeerNames == null || methodTargetPeerNames.isEmpty()) {
            LOGGER.debug("No target peer addresses defined in method field");
            var contractInfoTargetPeerNames = this.contractInfo.getTargetPeerNames();
            if (contractInfoTargetPeerNames == null || contractInfoTargetPeerNames.isEmpty()) {
                LOGGER.debug("No target peer addresses registered in contract info, defaulting to all peers known at the channel");
                return null;
            } else {
                return contractInfoTargetPeerNames;
            }
        } else {
            return methodTargetPeerNames;
        }
    }
}
