package org.blockchainnative.ethereum.metadata;

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
 * It is recommended to use {@link org.blockchainnative.ethereum.builder.EthereumContractInfoBuilder} to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.ethereum.EthereumContractWrapperGenerator
 * @since 1.0
 */
public class EthereumEventInfo extends EventInfo<EthereumEventFieldInfo, EthereumEventParameterInfo> {
    private final AbiDefinition abiDefinition;

    /**
     * Constructs a new {@code EthereumEventInfo}
     *
     * @param name                        name of the event as specified in the ABI
     * @param method                      method on the contract interface representing the event
     * @param ethereumEventParameterInfos {@code EthereumEventParameterInfo} objects
     * @param ethereumEventFieldInfos     {@code EthereumEventFieldInfo} objects
     * @param abiDefinition               ABI definition representing the smart contract event
     */
    public EthereumEventInfo(String name, Method method, Collection<EthereumEventParameterInfo> ethereumEventParameterInfos, Collection<EthereumEventFieldInfo> ethereumEventFieldInfos, AbiDefinition abiDefinition) {
        super(name, method, ethereumEventParameterInfos, ethereumEventFieldInfos);
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

        if (!(o instanceof EthereumEventInfo)) return false;

        EthereumEventInfo that = (EthereumEventInfo) o;

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
