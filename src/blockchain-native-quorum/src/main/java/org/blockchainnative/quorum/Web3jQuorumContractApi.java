package org.blockchainnative.quorum;

import io.reactivex.Observable;
import org.blockchainnative.quorum.metadata.QuorumContractInfo;
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
 * Wraps the calls to the Web3j Quorum API for a given contract.
 *
 * @author Matthias Veit
 * @since 1.1
 */
public interface Web3jQuorumContractApi {

    /**
     * Creates an observable for a given event.
     *
     * @param eventName  name of the Quorum smart contract event
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
     * @throws IOException in case of errors during the communication with the Quorum node
     */
    Result<List<Type>> executeFunctionCall(Function function) throws IOException;

    /**
     * Executes a smart contract Function in a transaction.
     *
     * @param function   Web3j smart contract Function
     * @param gasLimit   gas limit
     * @param value      amount of Ether (in Wei) to be transferred
     * @param privateFor list of base64 encoded public keys of the nodes which should be able to read the transaction
     *                   (can be null)
     * @return raw function result
     * @throws IOException          in case of errors during the communication with the Quorum node
     * @throws TransactionException in case the transaction failed to complete in a timely manner
     */
    Result<List<Type>> executeFunctionCallTransaction(Function function, BigInteger gasLimit, BigInteger value, List<String> privateFor) throws IOException, TransactionException;

    /**
     * Deploys the Quorum smart contract.
     *
     * @param constructorArguments constructor arguments
     * @param gasLimit             gas limit
     * @param value                amount of Ether (in Wei) to be transferred
     * @param privateFor           list of base64 encoded public keys of the nodes which should be able to use the
     *                             contract (can be null)
     * @return {@code Result} containing the address of the newly deployed contract.
     * @throws IOException          in case of errors during the communication with the Quorum node
     * @throws TransactionException in case the transaction failed to complete in a timely manner
     */
    Result<String> executeDeployTransaction(List<Type> constructorArguments, BigInteger gasLimit, BigInteger value, List<String> privateFor) throws IOException, TransactionException;

    /**
     * Sets the {@code QuorumContractInfo} the {@code Web3jQuorumContractApi} operates on
     *
     * @param contractInfo {@code QuorumContractInfo}
     */
    void setContractInfo(QuorumContractInfo<?> contractInfo);
}
