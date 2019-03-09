package org.blockchainnative.ethereum.transactions;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.transactions.Transaction;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Matthias Veit
 */
public abstract class EthereumBaseTransaction implements Transaction {
    protected BigInteger blockNumber;
    protected String blockHash;
    protected String hash;
    protected LocalDateTime timestamp;
    protected String sender;
    protected String recipient;
    protected byte[] data;
    protected BigInteger value;

    public EthereumBaseTransaction() {
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
    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EthereumBaseTransaction)) return false;

        EthereumBaseTransaction that = (EthereumBaseTransaction) o;

        if (!Objects.equals(blockNumber, that.blockNumber)) return false;
        if (!Objects.equals(blockHash, that.blockHash)) return false;
        if (!Objects.equals(hash, that.hash)) return false;
        if (!Objects.equals(timestamp, that.timestamp)) return false;
        if (!Objects.equals(sender, that.sender)) return false;
        if (!Objects.equals(recipient, that.recipient)) return false;
        if (!Arrays.equals(data, that.data)) {
            return false;
        }
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = blockNumber != null ? blockNumber.hashCode() : 0;
        result = 31 * result + (blockHash != null ? blockHash.hashCode() : 0);
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + (recipient != null ? recipient.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(data);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
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
