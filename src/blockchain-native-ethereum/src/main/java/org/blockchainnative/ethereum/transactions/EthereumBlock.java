package org.blockchainnative.ethereum.transactions;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a block on an Ethereum blockchain
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class EthereumBlock extends EthereumBaseBlock<EthereumTransaction> {

    public EthereumBlock() {
    }

    public EthereumBlock(BigInteger blockNumber, String blockHash, LocalDateTime timestamp, List<EthereumTransaction> ethereumTransactions) {
        super(blockNumber, blockHash, timestamp, ethereumTransactions);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("blockNumber", blockNumber)
                .append("blockHash", blockHash)
                .append("timestamp", timestamp)
                .append("transactions", transactions)
                .toString();
    }
}
