package org.blockchainnative.quorum.transactions;

import org.blockchainnative.ethereum.transactions.EthereumTransactionBuilder;
import org.blockchainnative.transactions.TransactionBuilder;
import org.web3j.quorum.Quorum;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.blockchainnative.ethereum.transactions.EthereumBaseTransactionRequest.NULL_RECIPIENT_ADDRESS;

/**
 * Creates a {@link org.blockchainnative.transactions.TransactionRequest} to be executed on a Quorum blockchain.
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class QuorumTransactionBuilder implements TransactionBuilder<QuorumTransactionBuilder, QuorumTransactionRequest> {

    private QuorumTransactionRequest transaction;
    
    public QuorumTransactionBuilder(Quorum quorum, TransactionManager transactionManager) {
        if (quorum == null)
            throw new IllegalArgumentException("Quorum client must not be null!");

        if (transactionManager == null) {
            throw new IllegalArgumentException("TransactionManager must not be null!");
        }

        transaction = new QuorumTransactionRequest(quorum, transactionManager);
    }

    /**
     * <b>NOT</b> supported by QuorumTransactionBuilder! Sender is determined through the specified TransactionManager.
     *
     * @param sender String identifying the sender
     * @return this {@code TransactionBuilder}
     */
    @Override
    public QuorumTransactionBuilder withSender(String sender) {
        throw new UnsupportedOperationException("QuorumTransactionBuilder sender address is determined by the specified TransactionManager");
    }

    /**
     * Sets the address of the recipient.
     *
     * @param recipient address of the recipient
     * @return this {@code QuorumTransactionBuilder}
     */
    @Override
    public QuorumTransactionBuilder withRecipient(String recipient) {
        transaction.setRecipient(recipient);
        return this;
    }

    /**
     * Sets the recipient of the transaction to '0x0000000000000000000000000000000000000000' {@link
     * org.blockchainnative.ethereum.transactions.EthereumBaseTransactionRequest#NULL_RECIPIENT_ADDRESS}
     *
     * @return this {@code QuorumTransactionBuilder}
     */
    public QuorumTransactionBuilder withNullRecipient() {
        return this.withRecipient(NULL_RECIPIENT_ADDRESS);
    }

    /**
     * Sets the data to be embedded in the in the transaction.
     *
     * @param data data to be stored in the transaction
     * @return this {@code TransactionBuilder}
     */
    @Override
    public QuorumTransactionBuilder withData(byte[] data) {
        transaction.setData(data);
        return this;
    }

    /**
     * Sets the gas limit for the transaction
     *
     * @param gasLimit gas limit for the transaction
     * @return this {@code TransactionBuilder}
     */
    public QuorumTransactionBuilder withGasLimit(BigInteger gasLimit) {
        transaction.setGasLimit(gasLimit);
        return this;
    }

    /**
     * Sets the amount of Ether to be transferred
     * Same as {@link EthereumTransactionBuilder#withValue(BigInteger)}
     *
     * @param etherInWei amount of Ether in Wei
     * @return this {@code TransactionBuilder}
     */
    public QuorumTransactionBuilder withEther(BigInteger etherInWei) {
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
    public QuorumTransactionBuilder withValue(BigInteger value) {
        return withEther(value);
    }

    /**
     * Sets the recipients who shall be able to see the transaction or null if it shall be a public transaction.
     *
     * @param recipients list of base64 encoded public keys of the recipient nodes
     * @return this {@code TransactionBuilder}
     */
    public QuorumTransactionBuilder withPrivateReceipients(List<String> recipients) {
        transaction.setPrivateFor(recipients);
        return this;
    }

    /**
     * Adds a recipient to the list of nodes who shall be able to see the transaction
     *
     * @param recipient base64 encoded public keys of the recipient node
     * @return this {@code TransactionBuilder}
     */
    public QuorumTransactionBuilder addPrivateRecipient(String recipient) {
        var list = transaction.getPrivateFor() != null ? transaction.getPrivateFor() : new ArrayList<String>();
        list.add(recipient);
        transaction.setPrivateFor(list);
        return this;
    }

    @Override
    public QuorumTransactionRequest build() {

        transaction.validate();

        return transaction;
    }
}
