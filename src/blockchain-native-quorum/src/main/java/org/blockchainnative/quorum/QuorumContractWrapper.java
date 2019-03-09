package org.blockchainnative.quorum;

import io.reactivex.Observable;
import org.blockchainnative.AbstractContractWrapper;
import org.blockchainnative.SpecialMethodDelegate;
import org.blockchainnative.convert.TypeConverters;

import org.blockchainnative.exceptions.ContractCallException;
import org.blockchainnative.exceptions.ContractDeploymentException;
import org.blockchainnative.metadata.Event;
import org.blockchainnative.metadata.Result;
import org.blockchainnative.quorum.metadata.*;
import org.blockchainnative.util.ReflectionUtil;
import org.blockchainnative.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.gas.ContractGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


/**
 * Base wrapper class for Quorum smart contracts used by {@link QuorumContractWrapperGenerator}
 *
 * @author Matthias Veit
 * @see QuorumContractWrapperGenerator
 * @since 1.1
 */
public class QuorumContractWrapper extends AbstractContractWrapper<QuorumContractInfo<?>, QuorumMethodInfo, QuorumParameterInfo, QuorumEventInfo, QuorumEventFieldInfo, QuorumEventParameterInfo> {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuorumContractWrapper.class);

    private final Web3jQuorumContractApi contractApi;
    private final QuorumArgumentConverter argumentConverter;
    private final ContractGasProvider contractGasProvider;

    public QuorumContractWrapper(QuorumContractInfo<?> contractInfo, Web3jQuorumContractApi contractApi, ContractGasProvider contractGasProvider, TypeConverters typeConverters) {
        super(contractInfo);
        this.contractApi = contractApi;
        this.contractGasProvider = contractGasProvider;
        this.argumentConverter = new QuorumArgumentConverterImpl(typeConverters);
    }

    public QuorumContractWrapper(QuorumContractInfo<?> contractInfo, Web3jQuorumContractApi contractApi, ContractGasProvider contractGasProvider, QuorumArgumentConverter argumentConverter) {
        super(contractInfo);
        this.contractApi = contractApi;
        this.contractApi.setContractInfo(contractInfo);
        this.contractGasProvider = contractGasProvider;
        this.argumentConverter = argumentConverter;
    }

    /**
     * Returns a mapping of special method names to their actions. <br>
     * {@code QuorumContractWrapper} registers a single mapping, {@link Constants#DEPLOYMENT_METHOD} to {@link QuorumContractWrapper#deploy(QuorumMethodInfo, Object[])}
     *
     * @return mapping of special method names to their actions.
     */
    @Override
    protected Map<String, SpecialMethodDelegate<QuorumMethodInfo>> getSpecialMethods() {
        return new HashMap<>() {{
            put(Constants.DEPLOYMENT_METHOD, (methodInfo, args) -> deploy(methodInfo, args));
        }};
    }

    /**
     * Creates an event observable for the given event method
     *
     * @param eventInfo {@code QuorumEventInfo} describing the corresponding smart contract event.
     * @param arguments arguments of the smart contract interface method
     * @return observable containing the events emitted by the smart contract converted to the expected type
     */
    @Override
    protected Observable<Object> createEventObservable(QuorumEventInfo eventInfo, Object[] arguments) {
        LOGGER.info("Preparing event observable '{}'", eventInfo.getEventName());

        var from = extractBlockParameterFrom(eventInfo, arguments);
        var to = extractBlockParameterTo(eventInfo, arguments);

        var observable = this.contractApi.getEventObservable(
                eventInfo.getEventName(), argumentConverter.getInputParameterTypesReferences(eventInfo.getAbiDefinition()), from, to);

        return observable.map(rawEvent -> {
            var eventData = this.argumentConverter.createEventObject(eventInfo, rawEvent.getData());

            if (ReflectionUtil.usesEventWrapper(eventInfo.getMethod())) {
                return new Event<>(eventData, rawEvent.getBlockHash(), rawEvent.getTransactionHash());
            } else {
                return eventData;
            }
        });
    }

    /**
     * Invokes the given smart contract method while submitting a transaction to the Ethereum blockchain.
     *
     * @param methodInfo {@code QuorumMethodInfo} describing the corresponding smart contract method.
     * @param arguments  arguments of the smart contract interface method
     * @return actual smart contract method result converted to the expected type and wrapped as {@link Future}
     */
    @Override
    protected Future<Object> invokeMethod(QuorumMethodInfo methodInfo, Object[] arguments) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Preparing function call '{}()'", methodInfo.getContractMethodName());

            var methodArguments = extractContractMethodParameters(methodInfo, arguments);
            var parameterInfos = extractContractMethodParameterInfos(methodInfo);

            LOGGER.debug("Converting arguments...");
            var convertedArgs = argumentConverter.convertArguments(parameterInfos, methodArguments);
            LOGGER.debug("Arguments: {}", convertedArgs.stream().map(x -> String.format("'%s'", x)).collect(Collectors.joining(", ")));

            var outputParameterTypes = argumentConverter.getOutputParameterTypeReferences(methodInfo.getAbi());

            var function = new org.web3j.abi.datatypes.Function(methodInfo.getContractMethodName(), convertedArgs, outputParameterTypes);

            Result<List<org.web3j.abi.datatypes.Type>> functionCallResult;
            try {
                var gasLimit = extractGasLimit(methodInfo, arguments);
                var value = extractEtherValue(methodInfo, arguments);
                var privateFor = getPrivateFor(methodInfo, arguments);

                functionCallResult = this.contractApi.executeFunctionCallTransaction(function, gasLimit, value, privateFor);

            } catch (IOException | TransactionException e) {
                var message = String.format("Failed to invoke function '%s' of contract '%s'!", methodInfo.getContractMethodName(), contractInfo.getContractClass().getName());
                LOGGER.error(message, e);
                throw new ContractCallException(message, e);
            }

            LOGGER.debug("Converting result to target type...");

            var convertedResult = argumentConverter.convertMethodResult(methodInfo, functionCallResult.getData());

            if (ReflectionUtil.usesResultWrapper(methodInfo.getMethod())) {
                return new Result<>(convertedResult, functionCallResult.getBlockHash(), functionCallResult.getTransactionHash());
            } else {
                return convertedResult;
            }
        });
    }

    /**
     * Invokes the given smart contract method without creating a transaction on the Ethereum blockchain.
     *
     * @param methodInfo {@code QuorumMethodInfo} describing the corresponding smart contract method.
     * @param arguments  arguments of the smart contract interface method
     * @return actual smart contract method result converted to the expected type and wrapped as {@link Future}
     */
    @Override
    protected Future<Object> invokeReadOnlyMethod(QuorumMethodInfo methodInfo, Object[] arguments) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Preparing readonly function call '{}()'", methodInfo.getContractMethodName());

            var methodArguments = extractContractMethodParameters(methodInfo, arguments);
            var parameterInfos = extractContractMethodParameterInfos(methodInfo);

            LOGGER.debug("Converting arguments...");
            var convertedArgs = argumentConverter.convertArguments(parameterInfos, methodArguments);
            LOGGER.debug("Arguments: {}", convertedArgs.stream().map(x -> String.format("'%s'", x)).collect(Collectors.joining(", ")));

            var outputParameterTypes = argumentConverter.getOutputParameterTypeReferences(methodInfo.getAbi());

            Result<List<org.web3j.abi.datatypes.Type>> functionCallResult;
            try {

                functionCallResult = this.contractApi.executeFunctionCall(
                        new org.web3j.abi.datatypes.Function(methodInfo.getContractMethodName(), convertedArgs, outputParameterTypes));
            } catch (IOException e) {
                var message = String.format("Failed to invoke readonly function '%s' of contract '%s'!", methodInfo.getContractMethodName(), contractInfo.getContractClass().getName());
                LOGGER.error(message, e);
                throw new ContractCallException(message, e);
            }

            LOGGER.debug("Converting result to target type...");
            var convertedResult = argumentConverter.convertMethodResult(methodInfo, functionCallResult.getData());

            if (ReflectionUtil.usesResultWrapper(methodInfo.getMethod())) {
                return new Result<>(convertedResult);
            } else {
                return convertedResult;
            }
        });
    }

    private Future<Object> deploy(QuorumMethodInfo methodInfo, Object[] arguments) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Preparing to deploy contract '{}' ({})", this.contractInfo.getIdentifier(), this.contractInfo.getContractClass().getName());

            if (!StringUtil.isNullOrEmpty(this.contractInfo.getContractAddress())) {
                var message = String.format("Cannot deploy contract '%s', contract address already set in contract info.", contractInfo.getContractClass().getName());
                LOGGER.error(message);
                throw new ContractDeploymentException(message);
            }

            if (StringUtil.isNullOrEmpty(this.contractInfo.getBinary())) {
                var message = String.format("Cannot deploy contract '%s', contract binary is not set in contract info.", contractInfo.getContractClass().getName());
                LOGGER.error(message);
                throw new ContractDeploymentException(message);
            }

            var deploymentArguments = extractContractMethodParameters(methodInfo, arguments);
            var parameterInfos = extractContractMethodParameterInfos(methodInfo);

            var gasLimit = extractGasLimit(methodInfo, arguments);
            var value = extractEtherValue(methodInfo, arguments);
            var privateFor = getPrivateFor(methodInfo, arguments);

            try {
                var deploymentResult = this.contractApi.executeDeployTransaction(argumentConverter.convertArguments(parameterInfos, deploymentArguments), gasLimit, value, privateFor);
                var address = deploymentResult.getData();

                this.contractInfo.setContractAddress(address);

                if (ReflectionUtil.usesResultWrapper(methodInfo.getMethod())) {
                    return new Result<>(address, deploymentResult.getBlockHash(), deploymentResult.getTransactionHash());
                } else {
                    return address;
                }

            } catch (IOException | TransactionException e) {
                throw new ContractDeploymentException(String.format("Failed to deploy contract '%s'", contractInfo.getContractClass().getName()), e);
            }
        });
    }


    private DefaultBlockParameter extractBlockParameterFrom(QuorumEventInfo eventInfo, Object[] arguments) {
        var fromBlock = extractSpecialArgument(eventInfo, Constants.FROM_BLOCK_ARGUMENT, arguments);
        if (fromBlock instanceof DefaultBlockParameter) {
            return (DefaultBlockParameter) fromBlock;
        }
        return DefaultBlockParameterName.LATEST;
    }

    private DefaultBlockParameter extractBlockParameterTo(QuorumEventInfo eventInfo, Object[] arguments) {
        var toBlock = extractSpecialArgument(eventInfo, Constants.TO_BLOCK_ARGUMENT, arguments);
        if (toBlock instanceof DefaultBlockParameter) {
            return (DefaultBlockParameter) toBlock;
        }
        return DefaultBlockParameterName.LATEST;
    }

    private List<String> extractPrivateFor(QuorumMethodInfo eventInfo, Object[] arguments) {
        var privateFor = extractSpecialArgument(eventInfo, Constants.PRIVATE_FOR_ARGUMENT, arguments);
        if (privateFor == null){
            return null;
        }

        if(privateFor instanceof Collection){
            return ((Collection<?>) privateFor).stream().map(Object::toString).collect(Collectors.toList());
        }else {
            var message = String.format("Unexpected type of additional field '%s'. Given: '%s', expected: java.util.Collection<String>.", Constants.PRIVATE_FOR_ARGUMENT, privateFor.getClass().getName());
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    private BigInteger extractGasLimit(QuorumMethodInfo methodInfo, Object[] arguments) {
        return extractBigInteger(methodInfo, Constants.GAS_LIMIT_ARGUMENT, contractGasProvider.getGasLimit(methodInfo.getContractMethodName()), arguments);
    }

    private BigInteger extractEtherValue(QuorumMethodInfo methodInfo, Object[] arguments) {
        return extractBigInteger(methodInfo, Constants.WEI_VALUE_ARGUMENT, null, arguments);
    }

    private BigInteger extractBigInteger(QuorumMethodInfo methodInfo, String parameterName, BigInteger defaultValue, Object[] arguments) {
        var gasPrice = extractSpecialArgument(methodInfo, parameterName, arguments);
        if (gasPrice instanceof BigInteger) {
            return (BigInteger) gasPrice;
        } else if (gasPrice instanceof Number) {
            return BigInteger.valueOf((((Number) gasPrice).longValue()));
        } else {
            return defaultValue;
        }
    }

    private List<String> getPrivateFor(QuorumMethodInfo methodInfo, Object[] arguments) {
        var methodPrivateFor = extractPrivateFor(methodInfo, arguments);
        if (methodPrivateFor == null || methodPrivateFor.isEmpty()) {
            LOGGER.debug("No private transaction targets defined in method arguments");
            var contractPrivateFor = this.contractInfo.getPrivateFor();
            if (contractPrivateFor == null || contractPrivateFor.isEmpty()) {
                LOGGER.debug("No private transaction targets registered in contract info, transaction will be public");
                return null;
            } else {
                return contractPrivateFor;
            }
        } else {
            return methodPrivateFor;
        }
    }

}
