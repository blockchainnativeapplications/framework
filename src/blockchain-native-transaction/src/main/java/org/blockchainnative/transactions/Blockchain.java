package org.blockchainnative.transactions;

import io.reactivex.Observable;

import java.math.BigInteger;

/**
 * Basic functions to interact with blocks and transactions stored on a blockchain
 *
 * @author Matthias Veit
 * @since 1.1
 */
public interface Blockchain<TBlock extends Block, TTransaction extends Transaction> {

    /**
     * Gets a block by its block number
     *
     * @param blockNumber number of the block
     * @return block
     * @throws org.blockchainnative.transactions.exceptions.BlockchainException in case an error during retrieval occurs
     *                                                                          or no block with the given number is
     *                                                                          found
     */
    TBlock getBlockByNumber(BigInteger blockNumber);

    /**
     * Gets a block by its hash value
     *
     * @param blockHash hex string representing the block's hash value
     * @return block
     * @throws org.blockchainnative.transactions.exceptions.BlockchainException in case an error during retrieval occurs
     *                                                                          or no block with the given hash is
     *                                                                          found
     */
    TBlock getBlockByHash(String blockHash);

    /**
     * Returns the blockchain's latest block
     *
     * @return block representing the head of the blockchain
     * @throws org.blockchainnative.transactions.exceptions.BlockchainException in case an error during retrieval
     *                                                                          occurs
     */
    TBlock getLatestBlock();

    /**
     * Gets a transaction by its hash value.
     *
     * @param transactionHash hex string representing the transaction's hash value
     * @return transaction
     * @throws org.blockchainnative.transactions.exceptions.BlockchainException in case an error during retrieval occurs
     *                                                                          or no transaction with the given hash is
     *                                                                          found
     */
    TTransaction getTransactionByHash(String transactionHash);

    /**
     * Returns an observable which allows subscribing to new blocks.
     *
     * @return Observable of new blocks
     */
    Observable<TBlock> getBlockObservable();

    /**
     * Returns an observable which allows subscribing to new transactions.
     *
     * @return Observable of new transactions
     */
    Observable<TTransaction> getTransactionObservable();
}
