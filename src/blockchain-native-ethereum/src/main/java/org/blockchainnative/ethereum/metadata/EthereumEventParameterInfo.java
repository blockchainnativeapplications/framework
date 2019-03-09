package org.blockchainnative.ethereum.metadata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.metadata.EventParameterInfo;

import java.lang.reflect.Parameter;

/**
 * Holds additional information about a parameter of a smart contract event method. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.ethereum.builder.EthereumContractInfoBuilder} to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.ethereum.EthereumContractWrapperGenerator
 * @since 1.0
 */
public class EthereumEventParameterInfo extends EventParameterInfo {

    /**
     * Constructs a new {@code EthereumEventParameterInfo}.
     *
     * @param parameter           parameter on the method on the smart contract interface representing the event.
     * @param parameterIndex      index of the parameter as it occurs in the method declaration
     * @param specialArgumentName special argument name for this parameter
     */
    public EthereumEventParameterInfo(Parameter parameter, int parameterIndex, String specialArgumentName) {
        super(parameter, parameterIndex, specialArgumentName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
