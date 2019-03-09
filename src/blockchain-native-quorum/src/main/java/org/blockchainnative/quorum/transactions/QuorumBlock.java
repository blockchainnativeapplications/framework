package org.blockchainnative.quorum.transactions;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.ethereum.transactions.EthereumBaseBlock;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a block on a Quorum blockchain
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class QuorumBlock extends EthereumBaseBlock<QuorumTransaction> {

    public QuorumBlock() {
    }

    public QuorumBlock(BigInteger blockNumber, String blockHash, LocalDateTime timestamp, List<QuorumTransaction> quorumTransactions) {
        super(blockNumber, blockHash, timestamp, quorumTransactions);
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
