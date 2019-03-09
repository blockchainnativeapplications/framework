package org.blockchainnative.transactions;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * Represents the basic data of a blockchain transaction.
 *
 * @author Matthias Veit
 * @since 1.1
 */
public interface Transaction {

    /**
     * Gets the number of the block containing the transaction or {@code null} if the blockchain does not number its blocks.
     *
     * @return number of the block containing the transaction
     */
    BigInteger getBlockNumber();

    /**
     * Gets the hash value of the block containing the transaction
     *
     * @return hex string representing the the hash value of the block containing the transaction
     */
    String getBlockHash();

    /**
     * Gets the hash value of the transaction
     *
     * @return hex string representing the the hash value of the transaction
     */
    String getHash();

    /**
     * Gets the timestamp at which the transaction was created. If this information is not available, the block's timestamp shall be returned.
     * If neither information is stored on the blockchain {@code null} shall be returned.
     *
     * @return timestamp at which the transaction was created
     */
    LocalDateTime getTimestamp();

    /**
     * Gets the identification of the transaction's sender
     *
     * @return identification of the transaction's sender
     */
    String getSender();

    /**
     * Gets the identification of the transaction's recipient
     *
     * @return identification of the transaction's recipient
     */
    String getRecipient();

    /**
     * Gets the amount of a blockchain's currency transferred in this transaction.
     *
     * @return amount of transferred currency
     */
    BigInteger getValue();

    /**
     * Gets the data stored in the transaction
     *
     * @return data stored in the transaction
     */
    byte[] getData();
}
