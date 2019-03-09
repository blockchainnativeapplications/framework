package org.blockchainnative.quorum.transactions;

import org.blockchainnative.ethereum.transactions.EthereumBaseTransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class QuorumTransactionRequest extends EthereumBaseTransactionRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuorumTransactionRequest.class);

    private List<String> privateFor;

    public QuorumTransactionRequest(Quorum quorum, TransactionManager transactionManager) {
        super(quorum, transactionManager);
    }

    public List<String> getPrivateFor() {
        return privateFor;
    }

    public void setPrivateFor(List<String> privateFor) {
        this.privateFor = privateFor;
    }

    @Override
    public String sendInternal() {
        List<String> previousPrivateFor = null;
        try {
            // set the transaction's privateFor list while preserving the previous state
            if (transactionManager instanceof ClientTransactionManager) {
                previousPrivateFor = ((ClientTransactionManager) transactionManager).getPrivateFor();
                ((ClientTransactionManager) transactionManager).setPrivateFor(getPrivateFor());
            }

            var transactionReceipt = super.send(recipient, encodeData(data), value, BigInteger.ZERO, gasLimit);

            return transactionReceipt.getTransactionHash();

        } catch (IOException | TransactionException e) {
            throw new org.blockchainnative.transactions.exceptions.TransactionException(
                    String.format("Failed to execute transaction with recipient '%s'", recipient), e);
        } finally {
            // reset the transactionManager's privateFor list if required
            if (transactionManager instanceof ClientTransactionManager
                    && previousPrivateFor != null) {
                ((ClientTransactionManager) transactionManager).setPrivateFor(previousPrivateFor);
            }
        }
    }

    @Override
    protected void validateProperties() {
        validateRecipient();
        validateGasLimit();
        validateValue();
        validateData();
        validatePrivateFor();
    }

    protected void validatePrivateFor(){
        LOGGER.debug("Private for: {}", privateFor != null ? privateFor.stream().collect(Collectors.joining(", ")) : "<null>");
    }
}
