/*
    This file is based on the Web3j's PollingTransactionReceiptProcessor
    https://github.com/web3j/web3j/blob/master/core/src/main/java/org/web3j/tx/response/PollingTransactionReceiptProcessor.java

    Original Copyright 2016 Conor Svensson

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.blockchainnative.ethereum;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

import static org.web3j.tx.TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH;
import static org.web3j.tx.TransactionManager.DEFAULT_POLLING_FREQUENCY;

/**
 * Allows to specifiy a minimum number of blocks to wait for, before considering a Transaction to be mined.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public class ConfigurablePollingTransactionReceiptProcessor extends TransactionReceiptProcessor {
    public static final int DEFAULT_CONFIRMATION_BLOCKS = 12;

    private final Web3j web3j;
    private final BigInteger minimumNumberOfConfirmationBlocks;
    private final long sleepDuration;
    private final int attempts;

    /**
     * Creates a new {@code ConfigurablePollingTransactionReceiptProcessor}
     *
     * @param web3j web3j api
     */
    public ConfigurablePollingTransactionReceiptProcessor(Web3j web3j) {
        this(web3j, DEFAULT_POLLING_FREQUENCY, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH, DEFAULT_CONFIRMATION_BLOCKS);
    }

    /**
     * Creates a new {@code ConfigurablePollingTransactionReceiptProcessor}
     *
     * @param web3j web3j api
     * @param minimumNumberOfConfirmationBlocks minimum number of blocks to wait for, default is 12
     */
    public ConfigurablePollingTransactionReceiptProcessor(Web3j web3j, int minimumNumberOfConfirmationBlocks) {
        this(web3j, DEFAULT_POLLING_FREQUENCY, DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH, minimumNumberOfConfirmationBlocks);
    }

    /**
     * Creates a new {@code ConfigurablePollingTransactionReceiptProcessor}
     *
     * @param web3j web3j api
     * @param sleepDuration duration to sleep between polling attempts, default is 15 seconds
     * @param attempts number of attempts to poll for a transaction receipt before considering it as failed, default is 40
     * @param minimumNumberOfConfirmationBlocks minimum number of blocks to wait for, default is 12
     */
    public ConfigurablePollingTransactionReceiptProcessor(Web3j web3j, long sleepDuration, int attempts, int minimumNumberOfConfirmationBlocks) {
        super(web3j);
        this.web3j = web3j;
        this.sleepDuration = sleepDuration;
        this.attempts = attempts;
        this.minimumNumberOfConfirmationBlocks = BigInteger.valueOf(minimumNumberOfConfirmationBlocks);
    }

    @Override
    public TransactionReceipt waitForTransactionReceipt(
            String transactionHash)
            throws IOException, TransactionException {

        return getTransactionReceipt(transactionHash, sleepDuration, attempts);
    }

    private TransactionReceipt getTransactionReceipt(
            String transactionHash, long sleepDuration, int attempts)
            throws IOException, TransactionException {

        var receiptOptional = sendTransactionReceiptRequest(transactionHash);
        for (var i = 0; i < attempts; i++) {
            if (!receiptOptional.isPresent()) {
                try {
                    Thread.sleep(sleepDuration);
                } catch (InterruptedException e) {
                    throw new TransactionException(e);
                }
                receiptOptional = sendTransactionReceiptRequest(transactionHash);
            } else {

                var receipt = receiptOptional.get();
                var latestBlockNumber = getLatestBlockNumber();

                for(var j = 0; j < minimumNumberOfConfirmationBlocks.longValue(); j ++) {
                    try {
                        Thread.sleep(JsonRpc2_0Web3j.DEFAULT_BLOCK_TIME);
                    } catch (InterruptedException e) {
                        throw new TransactionException(e);
                    }

                    if (receipt.getBlockNumber().add(minimumNumberOfConfirmationBlocks).compareTo(latestBlockNumber) >= 0) {
                        return receipt;
                    }

                }
                throw new TransactionException("Transaction receipt was generated but could not get enough confirmation blocks after  "
                        + ((JsonRpc2_0Web3j.DEFAULT_BLOCK_TIME *  minimumNumberOfConfirmationBlocks.longValue()) / 1000
                        + " seconds for transaction: " + transactionHash));
            }
        }

        throw new TransactionException("Transaction receipt was not generated after "
                + ((sleepDuration * attempts) / 1000
                + " seconds for transaction: " + transactionHash));
    }

    // Because the super classes method is is not defined as protected, we need to work around that
    private Optional<TransactionReceipt> sendTransactionReceiptRequest(String transactionHash) throws IOException, TransactionException {
        var transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
        if (transactionReceipt.hasError()) {
            throw new TransactionException("Error processing request: " + transactionReceipt.getError().getMessage());
        }

        return transactionReceipt.getTransactionReceipt();
    }

    private BigInteger getLatestBlockNumber() throws IOException, TransactionException {
        var blockNumber = web3j.ethBlockNumber().send();
        if (blockNumber.hasError()) {
            throw new TransactionException("Error processing request: " + blockNumber.getError().getMessage());
        }

        return blockNumber.getBlockNumber();
    }
}
