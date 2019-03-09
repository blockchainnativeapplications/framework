package org.blockchainnative.ethereum;

import io.reactivex.Observable;
import org.blockchainnative.ethereum.metadata.EthereumContractInfo;
import org.blockchainnative.metadata.Event;
import org.blockchainnative.metadata.Result;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * Wraps the calls to the Web3j Ethereum API for a given contract.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public interface Web3ContractApi {

    /**
     * Creates an observable for a given event.
     *
     * @param eventName  name of the Ethereum smart contract event
     * @param eventTypes {@code TypeReference} objects representing the event's input types
     * @param fromBlock  defines the starting block from which events want to be received
     * @param toBlock    defines the end block until which events want to be received
     * @return raw event observable
     */
    Observable<Event<EventValues>> getEventObservable(String eventName, List<TypeReference<?>> eventTypes, DefaultBlockParameter fromBlock, DefaultBlockParameter toBlock);

    /**
     * Executes a smart contract Function without submitting a transaction.
     *
     * @param function Web3j smart contract Function
     * @return raw function result
     * @throws IOException in case of errors during the communication with the Ethereum node
     */
    Result<List<Type>> executeFunctionCall(Function function) throws IOException;

    /**
     * Executes a smart contract Function in a transaction.
     *
     * @param function Web3j smart contract Function
     * @param gasPrice gas price in Wei
     * @param gasLimit gas limit
     * @param value    amount of Ether (in Wei) to be transferred
     * @return raw function result
     * @throws IOException          in case of errors during the communication with the Ethereum node
     * @throws TransactionException in case the transaction failed to complete in a timely manner
     */
    Result<List<Type>> executeFunctionCallTransaction(Function function, BigInteger gasPrice, BigInteger gasLimit, BigInteger value) throws IOException, TransactionException;

    /**
     * Deploys the Ethereum smart contract.
     *
     * @param constructorArguments constructor arguments
     * @param gasPrice             gas price in Wei
     * @param gasLimit             gas limit
     * @param value                amount of Ether (in Wei) to be transferred
     * @return {@code Result} containing the address of the newly deployed contract.
     * @throws IOException          in case of errors during the communication with the Ethereum node
     * @throws TransactionException in case the transaction failed to complete in a timely manner
     */
    Result<String> executeDeployTransaction(List<Type> constructorArguments, BigInteger gasPrice, BigInteger gasLimit, BigInteger value) throws IOException, TransactionException;

    /**
     * Sets the {@code EthereumContractInfo} the {@code Web3ContractApi} operates on
     *
     * @param contractInfo {@code EthereumContractInfo}
     */
    void setContractInfo(EthereumContractInfo<?> contractInfo);
}
