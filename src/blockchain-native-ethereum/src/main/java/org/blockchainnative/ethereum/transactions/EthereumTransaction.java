package org.blockchainnative.ethereum.transactions;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents a transaction on an Ethereum blockchain.
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class EthereumTransaction extends EthereumBaseTransaction {

    public EthereumTransaction() {
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("blockNumber", blockNumber)
                .append("blockHash", blockHash)
                .append("hash", hash)
                .append("timestamp", timestamp)
                .append("sender", sender)
                .append("recipient", recipient)
                .append("data", data)
                .append("value", value)
                .toString();
    }
}
