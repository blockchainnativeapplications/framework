package org.blockchainnative.ethereum.transactions;

import org.blockchainnative.transactions.TransactionRequest;
import org.blockchainnative.transactions.exceptions.TransactionValidationException;
import org.blockchainnative.util.StringUtil;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

/**
 * @author Matthias Veit
 */
public abstract class EthereumBaseTransactionRequest extends ManagedTransaction implements TransactionRequest {
    public static final String NULL_RECIPIENT_ADDRESS = "0x0000000000000000000000000000000000000000";

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumBaseTransactionRequest.class);

    protected static final String INVALID_TRANSACTION_PREFIX = "Invalid Transaction: ";

    protected String recipient;
    protected byte[] data;
    protected BigInteger value;
    protected BigInteger gasLimit;

    public EthereumBaseTransactionRequest(Web3j web3j, TransactionManager transactionManager) {
        super(web3j, transactionManager);
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getRecipient() {
        return recipient;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String send() {

        // validate the transaction before executing it
        validate();

        return sendInternal();
    }

    @Override
    public CompletableFuture<String> sendAsync() {
        //return CompletableFuture.supplyAsync(() -> send()); // TODO executor?
        return CompletableFuture.completedFuture(send());
    }

    protected abstract String sendInternal();

    protected abstract void validateProperties();

    public void validate() {
        LOGGER.debug("Validating transaction...");

        validateProperties();
    }

    protected void validateRecipient() {
        if (StringUtil.isNullOrEmpty(recipient)) {
            var message = String.format("%sRecipient is not set", INVALID_TRANSACTION_PREFIX);
            LOGGER.error(message);
            throw new TransactionValidationException(message);
        }

        if (!recipient.startsWith("0x")) {
            recipient = "0x" + recipient;
        }

        if (!recipient.matches("^0x[0-9a-fA-F]{40}$")) {
            var message = String.format("%sInvalid recipient! Recipient address needs to be a byte hex string!", INVALID_TRANSACTION_PREFIX);
            LOGGER.error(message);
            throw new TransactionValidationException(message);
        }

        LOGGER.debug("Transaction recipient: {}", recipient);
    }

    protected void validateGasLimit() {
        if (gasLimit == null || gasLimit.signum() == -1) {
            var defaultGasLimit = DefaultGasProvider.GAS_LIMIT;
            if (gasLimit == null) {
                var message = String.format("Gas limit not set, defaulting to '%s'", defaultGasLimit.toString());
                LOGGER.debug(message);
            } else {
                var message = String.format("Negative gas limit (%s) not allowed, defaulting to '%s'", gasLimit.toString(), defaultGasLimit.toString());
                LOGGER.warn(message);
            }
            gasLimit = defaultGasLimit;
        } else {
            LOGGER.debug("Gas limit: {}", gasLimit);
        }
    }

    protected void validateValue() {
        if (value == null) {
            LOGGER.debug("No Ether to be sent");
            value = BigInteger.ZERO;
        } else if (value.signum() == -1) {
            var message = String.format("%Invalid amount of Ether to be transferred: Value is negative!", INVALID_TRANSACTION_PREFIX);
            LOGGER.error(message);
            throw new TransactionValidationException(message);
        } else {
            LOGGER.debug("Value: {} Wei", gasLimit);
        }
    }

    protected void validateData() {
        if (data == null) {
            LOGGER.debug("No data to embedded");
            data = new byte[0];
        } else {
            LOGGER.debug("Data ({} byte{}): 0x{}{}",
                    data.length,
                    data.length != 1 ? "s" : "",
                    Hex.toHexString(data, 0, Math.min(data.length, 50)),
                    data.length > 50 ? "..." : "");
        }
    }

    protected String encodeData(byte[] data) {
        return data != null || data.length == 0 ? "0x" + Hex.toHexString(data) : "0x0";
    }

}
