package org.blockchainnative.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Wrapper type for smart contract events which allows retrieving additional information about the blockchain when
 * an event occurs.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.ContractWrapperGenerator
 * @since 1.0
 */
public class Event<T> {
    protected final String blockHash;
    protected final String transactionHash;
    protected final T data;

    /**
     * Construct a new {@code Event} without additional blockchain information
     *
     * @param data actual event data
     */
    public Event(T data) {
        this(data, null, null);
    }

    /**
     * Construct a new {@code Event} with additional blockchain information
     *
     * @param data            actual event data
     * @param blockHash       hash of the block containing the transaction which caused the event
     * @param transactionHash hash of the transaction which caused the event
     */
    public Event(T data, String blockHash, String transactionHash) {
        this.data = data;
        this.blockHash = blockHash;
        this.transactionHash = transactionHash;
    }

    /**
     * Returns the hash of the block containing the transaction which caused the event.
     *
     * @return hash of the block containing the transaction which caused the event.
     */
    public String getBlockHash() {
        return blockHash;
    }

    /**
     * Returns the hash of the transaction which caused the event.
     *
     * @return hash of the transaction which caused the event.
     */
    public String getTransactionHash() {
        return transactionHash;
    }

    /**
     * Returns the actual event data.
     *
     * @return actual event data.
     */
    public T getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Event)) return false;

        Event<?> event = (Event<?>) o;

        return new EqualsBuilder()
                .append(blockHash, event.blockHash)
                .append(transactionHash, event.transactionHash)
                .append(data, event.data)
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
