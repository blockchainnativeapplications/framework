package org.blockchainnative.ethereum.transactions;

import org.blockchainnative.transactions.TransactionBuilder;
import org.web3j.protocol.Web3j;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

import static org.blockchainnative.ethereum.transactions.EthereumBaseTransactionRequest.NULL_RECIPIENT_ADDRESS;

/**
 * Creates a {@link org.blockchainnative.transactions.TransactionRequest} to be executed on a Ethereum blockchain.
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class EthereumTransactionBuilder implements TransactionBuilder<EthereumTransactionBuilder, EthereumTransactionRequest> {

    private EthereumTransactionRequest transaction;

    public EthereumTransactionBuilder(Web3j web3j, TransactionManager transactionManager) {
        if(web3j == null)
            throw new IllegalArgumentException("Web3j client must not be null!");

        if(transactionManager == null){
            throw new IllegalArgumentException("TransactionManager must not be null!");
        }

        transaction = new EthereumTransactionRequest(web3j, transactionManager);
    }

    /**
     * Specifies the identification of the transaction sender.
     * <b>NOT</b> supported by EthereumTransactionBuilder! Sender is determined through the specified TransactionManager.
     *
     * @param sender String identifying the sender
     * @return this {@code TransactionBuilder}
     */
    @Override
    public EthereumTransactionBuilder withSender(String sender) {
        throw new UnsupportedOperationException("EthereumTransactionBuilder sender address is determined by the specified TransactionManager");
    }

    /**
     * Sets the address of the recipient.
     *
     * @param recipient address of the recipient
     * @return this {@code QuorumTransactionBuilder}
     */
    @Override
    public EthereumTransactionBuilder withRecipient(String recipient) {
        transaction.setRecipient(recipient);
        return this;
    }

    /**
     * Sets the recipient of the transaction to '0x0000000000000000000000000000000000000000' {@link
     * org.blockchainnative.ethereum.transactions.EthereumBaseTransactionRequest#NULL_RECIPIENT_ADDRESS}
     *
     * @return this {@code QuorumTransactionBuilder}
     */
    public EthereumTransactionBuilder withNullRecipient() {
        return this.withRecipient(NULL_RECIPIENT_ADDRESS);
    }

    /**
     * Sets the data to be embedded in the in the transaction.
     *
     * @param data data to be stored in the transaction
     * @return this {@code TransactionBuilder}
     */
    @Override
    public EthereumTransactionBuilder withData(byte[] data) {
        transaction.setData(data);
        return this;
    }

    /**
     * Sets the gas price for the transaction
     *
     * @param gasPrice gas price for the transaction
     * @return this {@code TransactionBuilder}
     */
    public EthereumTransactionBuilder withGasPrice(BigInteger gasPrice){
        transaction.setGasPrice(gasPrice);
        return this;
    }

    /**
     * Sets the gas limit for the transaction
     *
     * @param gasLimit gas limit for the transaction
     * @return this {@code TransactionBuilder}
     */
    public EthereumTransactionBuilder withGasLimit(BigInteger gasLimit){
        transaction.setGasLimit(gasLimit);
        return this;
    }

    /**
     * Sets the amount of Ether to be transferred.
     * Same as {@link EthereumTransactionBuilder#withValue(BigInteger)}
     *
     * @param etherInWei amount of Ether in Wei
     * @return this {@code TransactionBuilder}
     */
    public EthereumTransactionBuilder withEther(BigInteger etherInWei){
        transaction.setValue(etherInWei);
        return this;
    }

    /**
     * Sets the amount of Ether to be transferred
     *
     * @param value amount of Ether in Wei
     * @return this {@code TransactionBuilder}
     */
    @Override
    public EthereumTransactionBuilder withValue(BigInteger value) {
        return withEther(value);
    }

    @Override
    public EthereumTransactionRequest build() {

        transaction.validate();

        return transaction;
    }
}
