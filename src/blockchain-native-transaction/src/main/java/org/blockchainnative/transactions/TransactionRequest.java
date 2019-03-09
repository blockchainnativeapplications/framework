package org.blockchainnative.transactions;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a blockchain transaction
 *
 * @author Matthias Veit
 * @since 1.1
 */
public interface TransactionRequest {

    /**
     * Executes the transaction and stores it on the blockchain.
     *
     * @return hash value of transaction
     */
    String send();

    /**
     * Executes the transaction asynchronous and stores it on the blockchain.
     *
     * @return hash value of transaction
     */
    CompletableFuture<String> sendAsync();
}
