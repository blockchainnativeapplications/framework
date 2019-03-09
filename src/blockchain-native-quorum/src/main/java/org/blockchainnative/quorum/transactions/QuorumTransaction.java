package org.blockchainnative.quorum.transactions;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.ethereum.transactions.EthereumBaseTransaction;

import java.util.List;
import java.util.Objects;

/**
 * Represents a transaction on a Quorum blockchain.
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class QuorumTransaction extends EthereumBaseTransaction {
    private boolean isPrivate;
    private List<String> privateFor;
    private String payloadHash;

    public QuorumTransaction() {
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    public List<String> getPrivateFor() {
        return privateFor;
    }

    public void setPrivateFor(List<String> privateFor) {
        this.privateFor = privateFor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuorumTransaction)) return false;
        if (!super.equals(o)) return false;

        QuorumTransaction that = (QuorumTransaction) o;

        if (isPrivate != that.isPrivate) return false;
        if (!Objects.equals(privateFor, that.privateFor)) return false;
        return Objects.equals(payloadHash, that.payloadHash);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isPrivate ? 1 : 0);
        result = 31 * result + (privateFor != null ? privateFor.hashCode() : 0);
        result = 31 * result + (payloadHash != null ? payloadHash.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("isPrivate", isPrivate)
                .append("privateFor", privateFor)
                .append("payloadHash", payloadHash)
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
