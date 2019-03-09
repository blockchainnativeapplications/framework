package org.blockchainnative.quorum;

import io.reactivex.Observable;
import org.blockchainnative.quorum.metadata.QuorumContractInfo;
import org.blockchainnative.exceptions.ContractCallException;
import org.blockchainnative.metadata.Event;
import org.blockchainnative.metadata.Result;
import org.blockchainnative.util.StringUtil;
import org.web3j.abi.*;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * @author Matthias Veit
 * @since 1.1
 */
public class Web3jQuorumContractApiImpl implements Web3jQuorumContractApi {

    private final Quorum quorum;
    private final TransactionManager transactionManager;
    private QuorumContractInfo<?> contractInfo;


    public Web3jQuorumContractApiImpl(Quorum quorum, TransactionManager transactionManager, QuorumContractInfo<?> contractInfo) {
        this.quorum = quorum;
        this.transactionManager = transactionManager;
        this.contractInfo = contractInfo;
    }

    @Override
    public Observable<Event<EventValues>> getEventObservable(String eventName, List<TypeReference<?>> eventTypes, DefaultBlockParameter fromBlock, DefaultBlockParameter toBlock) {
        var event = new org.web3j.abi.datatypes.Event(eventName, eventTypes);

        var address = getContractAddress();

        var filter = new EthFilter(fromBlock, toBlock, address);
        filter.addSingleTopic(EventEncoder.encode(event));

        return this.quorum.ethLogFlowable(filter).map(log -> {
            var eventValues = Contract.staticExtractEventParameters(event, log);
            var eventValuesWithBlockData = new Event<>(
                    eventValues, log.getBlockHash(), log.getTransactionHash());

            return eventValuesWithBlockData;
        }).toObservable();
    }

    @Override
    public Result<List<Type>> executeFunctionCall(Function function) throws IOException {
        var encodedFunction = FunctionEncoder.encode(function);
        var encodedResult = executeFunctionCall(encodedFunction);
        var output = FunctionReturnDecoder.decode(encodedResult, function.getOutputParameters());

        return new Result<>(output, null, null);
    }

    @Override
    public Result<List<Type>> executeFunctionCallTransaction(Function function, BigInteger gasLimit, BigInteger value, List<String> privateFor) throws IOException, TransactionException {
        var encodedFunction = FunctionEncoder.encode(function);
        var encodedResult = executeFunctionCall(encodedFunction);
        var output = FunctionReturnDecoder.decode(encodedResult, function.getOutputParameters());

        var transactionReceipt = executeTransaction(getContractAddress(), gasLimit, encodedFunction, value, privateFor);

        return new Result<>(output, transactionReceipt.getBlockHash(), transactionReceipt.getTransactionHash());
    }

    @Override
    public Result<String> executeDeployTransaction(List<Type> constructorArguments, BigInteger gasLimit, BigInteger value, List<String> privateFor) throws IOException, TransactionException {
        String encodedConstructor = FunctionEncoder.encodeConstructor(constructorArguments);
        var transactionReceipt = executeTransaction(null, gasLimit, getContractBinary() + encodedConstructor, value, privateFor);

        return new Result<>(transactionReceipt.getContractAddress(), transactionReceipt.getBlockHash(), transactionReceipt.getTransactionHash());
    }

    private String executeFunctionCall(String encodedFunction) throws IOException {
        return this.quorum.ethCall(
                Transaction.createEthCallTransaction(
                        transactionManager.getFromAddress(), getContractAddress(), encodedFunction), DefaultBlockParameterName.LATEST)
                .send().getValue();
    }

    private TransactionReceipt executeTransaction(String to, BigInteger gasLimit, String data, BigInteger value, List<String> privateFor) throws IOException, TransactionException {
        var transaction = new GeneralPurposeTransaction(quorum, transactionManager);
        var transactionReceipt = transaction.executeTransaction(to, data, value, gasLimit, privateFor);

        if (!transactionReceipt.isStatusOK()) {
            throw new ContractCallException(
                    String.format(
                            "Failed to execute transaction, status: '%s'.", transactionReceipt.getStatus()));
        }

        return transactionReceipt;
    }

    private String getContractAddress() {
        if (this.contractInfo == null) {
            throw new IllegalStateException("ContractInfo is not set");
        }

        var address = this.contractInfo.getContractAddress();
        if (StringUtil.isNullOrEmpty(address)) {
            throw new IllegalStateException("Contract address is not set in contract info");
        }

        if (!address.startsWith("0x")) {
            address = "0x" + address;
        }

        return address;
    }

    public String getContractBinary() {
        if (this.contractInfo == null) {
            throw new IllegalStateException("ContractInfo must not be null");
        }

        var binary = this.contractInfo.getBinary();
        if (StringUtil.isNullOrEmpty(binary)) {
            throw new IllegalStateException("Contract binary is not set in contract info");
        }

        return binary;
    }

    @Override
    public void setContractInfo(QuorumContractInfo<?> contractInfo) {
        this.contractInfo = contractInfo;
    }

    /**
     * This class solely exists because the executeTransaction() method is hidden in TransactionManager, however
     * ManagedTransaction.send() uses this functionality. We use this method to execute an Transaction and wait for it
     * to be mined.
     */
    private static class GeneralPurposeTransaction extends ManagedTransaction {

        protected GeneralPurposeTransaction(Web3j web3j, TransactionManager transactionManager) {
            super(web3j, transactionManager);
        }

        public TransactionReceipt executeTransaction(String to, String data, BigInteger value, BigInteger gasLimit, List<String> privateFor) throws IOException, TransactionException {
            value = ensureNotNull(value);
            gasLimit = ensureNotNull(gasLimit);

            List<String> previousPrivateFor = null;
            try {
                // set the transaction's privateFor list while preserving the previous state
                if (transactionManager instanceof ClientTransactionManager) {
                    previousPrivateFor = ((ClientTransactionManager) transactionManager).getPrivateFor();
                    ((ClientTransactionManager) transactionManager).setPrivateFor(privateFor);
                }

                return super.send(to, data, value, BigInteger.ZERO, gasLimit);

            } finally {
                // reset the transactionManager's privateFor list if required
                if (transactionManager instanceof ClientTransactionManager) {
                    ((ClientTransactionManager) transactionManager).setPrivateFor(previousPrivateFor);
                }
            }
        }
    }

    private static BigInteger ensureNotNull(BigInteger value) {
        return value == null ? BigInteger.ZERO : value;
    }
}
