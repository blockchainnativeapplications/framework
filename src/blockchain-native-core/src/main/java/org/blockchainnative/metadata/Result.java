package org.blockchainnative.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Wrapper type for smart contract methods which allows retrieving additional information about the blockchain when
 * the method was called.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.ContractWrapperGenerator
 * @since 1.0
 */
public class Result<T> {
    protected final String blockHash;
    protected final String transactionHash;
    protected final T data;

    /**
     * Construct a new {@code Result} without additional blockchain information
     *
     * @param data actual result data
     */
    public Result(T data) {
        this(data, null, null);
    }

    /**
     * Construct a new {@code Result} without additional blockchain information
     *
     * @param data            actual result data
     * @param blockHash       hash of the block containing the transaction in which the method has been called
     * @param transactionHash hash of the transaction in which the method has been called
     */
    public Result(T data, String blockHash, String transactionHash) {
        this.data = data;
        this.blockHash = blockHash;
        this.transactionHash = transactionHash;
    }

    /**
     * Returns the hash of the block containing the transaction in which the method has been called.
     *
     * @return hash of the block containing the transaction in which the method has been called
     */
    public String getBlockHash() {
        return blockHash;
    }

    /**
     * Returns the hash of the transaction in which the method has been called.
     *
     * @return hash of the transaction in which the method has been called.
     */
    public String getTransactionHash() {
        return transactionHash;
    }

    /**
     * Returns the actual method result data.
     *
     * @return actual method result data.
     */
    public T getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Result)) return false;

        Result<?> result = (Result<?>) o;

        return new EqualsBuilder()
                .append(blockHash, result.blockHash)
                .append(transactionHash, result.transactionHash)
                .append(data, result.data)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(blockHash)
                .append(transactionHash)
                .append(data)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("blockHash", blockHash)
                .append("transactionHash", transactionHash)
                .append("data", data)
                .toString();
    }
}
