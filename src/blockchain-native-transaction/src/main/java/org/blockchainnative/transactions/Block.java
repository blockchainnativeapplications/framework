package org.blockchainnative.transactions;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the basic data of a blockchain block.
 *
 * @author Matthias Veit
 * @since 1.1
 */
public interface Block {

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
     * Gets the timestamp at which the block was created or {@code null} if the information is not stored on the blockchain.
     *
     * @return timestamp at which the block was created
     */
    LocalDateTime getTimestamp();

    /**
     * Gets the list of transactions embedded in this block.
     *
     * @returnlist of transactions embedded in this block
     */
    List<? extends Transaction> getTransactions();
}
