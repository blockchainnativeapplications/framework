package org.blockchainnative.ethereum.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author Matthias Veit
 * @since 1.1
 */
public class EthereumTransactionRequest extends EthereumBaseTransactionRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumTransactionRequest.class);

    private BigInteger gasPrice;

    protected EthereumTransactionRequest(Web3j web3j, TransactionManager transactionManager) {
        super(web3j, transactionManager);
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    @Override
    public String sendInternal() {
        try {
            var transactionReceipt = super.send(recipient, encodeData(data), value, gasPrice, gasLimit);

            return transactionReceipt.getTransactionHash();
        } catch (IOException | TransactionException e) {
            throw new org.blockchainnative.transactions.exceptions.TransactionException(
                    String.format("Failed to execute transaction with recipient '%s'", recipient), e);
        }
    }

    @Override
    protected void validateProperties() {
        validateRecipient();
        validateGasLimit();
        validateGasPrice();
        validateValue();
        validateData();
    }

    protected void validateGasPrice(){
        if (gasPrice == null || gasPrice.signum() == -1) {
            var defaultGasPrice = DefaultGasProvider.GAS_PRICE;
            if (gasPrice == null) {
                var message = String.format("Gas price not set, defaulting to '%s'", defaultGasPrice.toString());
                LOGGER.debug(message);
            } else {
                var message = String.format("Negative gas price (%s) not allowed, defaulting to '%s'", gasPrice.toString(), defaultGasPrice.toString());
                LOGGER.warn(message);
            }
            gasPrice = defaultGasPrice;
        } else {
            LOGGER.debug("Gas price: {}", gasPrice);
        }
    }

}
