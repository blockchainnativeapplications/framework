package org.blockchainnative.transactions;

import java.math.BigInteger;

/**
 * Creates {@link TransactionRequest} objects to execute transactions on a blockchain.
 *
 * @author Matthias Veit
 * @since 1.1
 */
public interface TransactionBuilder<TSelf extends TransactionBuilder<TSelf, TTransaction>, TTransaction extends TransactionRequest> {

    /**
     * Specifies the identification of the transaction sender.
     *
     * @param sender String identifying the sender
     * @return this {@code TransactionBuilder}
     */
    TSelf withSender(String sender);

    /**
     * Sets the identification of the transaction recipient.
     *
     * @param recipient String identifying the recipient
     * @return this {@code TransactionBuilder}
     */
    TSelf withRecipient(String recipient);

    /**
     * Sets the data to be embedded in the in the transaction.
     *
     * @param data data to be stored in the transaction
     * @return this {@code TransactionBuilder}
     */
    TSelf withData(byte[] data);

    /**
     * Sets the amount of currency to be transferred in the transaction.
     *
     * @param value amount of currency to be transferred
     * @return this {@code TransactionBuilder}
     */
    TSelf withValue(BigInteger value);

    /**
     * Creates the {@link TTransaction} object containing the properties specified through the builder. <br> The {@code
     * TransactionBuilder} is responsible for validating the resulting transaction.
     *
     * <strong>IMPORTANT:</strong> the {@code TransactionBuilder} shall not be used after calling build once.
     *
     * @return {@link TTransaction} object containing the properties specified through the builder
     * @throws org.blockchainnative.transactions.exceptions.TransactionValidationException in case the resulting
     *                                                                                     transaction would be invalid
     */
    TTransaction build();

    @SuppressWarnings("unchecked")
    default TSelf self() {
        return (TSelf) this;
    }
}
