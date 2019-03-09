package org.blockchainnative.ethereum.transactions;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.transactions.Block;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author Matthias Veit
 */
public abstract class EthereumBaseBlock<TTransaction extends EthereumBaseTransaction> implements Block {
    protected BigInteger blockNumber;
    protected String blockHash;
    protected LocalDateTime timestamp;
    protected List<TTransaction> transactions;

    public EthereumBaseBlock() {
    }

    public EthereumBaseBlock(BigInteger blockNumber, String blockHash, LocalDateTime timestamp, List<TTransaction> transactions) {
        this.blockNumber = blockNumber;
        this.blockHash = blockHash;
        this.timestamp = timestamp;
        this.transactions = transactions;
    }

    @Override
    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    @Override
    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public List<TTransaction> getTransactions() { return this.transactions; }

    public void setTransactions(List<TTransaction> transactions) { this.transactions = transactions; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EthereumBaseBlock)) return false;

        EthereumBaseBlock<?> that = (EthereumBaseBlock<?>) o;

        if (!Objects.equals(blockNumber, that.blockNumber)) return false;
        if (!Objects.equals(blockHash, that.blockHash)) return false;
        if (!Objects.equals(timestamp, that.timestamp)) return false;
        return Objects.equals(transactions, that.transactions);
    }

    @Override
    public int hashCode() {
        int result = blockNumber != null ? blockNumber.hashCode() : 0;
        result = 31 * result + (blockHash != null ? blockHash.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (transactions != null ? transactions.hashCode() : 0);
        return result;
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
