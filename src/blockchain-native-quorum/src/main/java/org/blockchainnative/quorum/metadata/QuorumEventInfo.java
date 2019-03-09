package org.blockchainnative.quorum.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.metadata.EventInfo;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Holds additional information about a smart contract event. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.quorum.builder.QuorumContractInfoBuilder} to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.quorum.QuorumContractWrapperGenerator
 * @since 1.1
 */
public class QuorumEventInfo extends EventInfo<QuorumEventFieldInfo, QuorumEventParameterInfo> {
    private final AbiDefinition abiDefinition;

    /**
     * Constructs a new {@code QuorumEventInfo}
     *
     * @param name                        name of the event as specified in the ABI
     * @param method                      method on the contract interface representing the event
     * @param quorumEventParameterInfos {@code QuorumEventParameterInfo} objects
     * @param quorumEventFieldInfos     {@code QuorumEventFieldInfo} objects
     * @param abiDefinition               ABI definition representing the smart contract event
     */
    public QuorumEventInfo(String name, Method method, Collection<QuorumEventParameterInfo> quorumEventParameterInfos, Collection<QuorumEventFieldInfo> quorumEventFieldInfos, AbiDefinition abiDefinition) {
        super(name, method, quorumEventParameterInfos, quorumEventFieldInfos);
        this.abiDefinition = abiDefinition;
    }

    /**
     * Returns the ABI definition of the event
     *
     * @return ABI definition of the event
     */
    public AbiDefinition getAbiDefinition() {
        return abiDefinition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof QuorumEventInfo)) return false;

        QuorumEventInfo that = (QuorumEventInfo) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(abiDefinition, that.abiDefinition)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(abiDefinition)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
