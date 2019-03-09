package org.blockchainnative.quorum.metadata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.metadata.EventParameterInfo;

import java.lang.reflect.Parameter;

/**
 * Holds additional information about a parameter of a smart contract event method. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.quorum.builder.QuorumContractInfoBuilder} to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.quorum.QuorumContractWrapperGenerator
 * @since 1.1
 */
public class QuorumEventParameterInfo extends EventParameterInfo {

    /**
     * Constructs a new {@code QuorumEventParameterInfo}.
     *
     * @param parameter           parameter on the method on the smart contract interface representing the event.
     * @param parameterIndex      index of the parameter as it occurs in the method declaration
     * @param specialArgumentName special argument name for this parameter
     */
    public QuorumEventParameterInfo(Parameter parameter, int parameterIndex, String specialArgumentName) {
        super(parameter, parameterIndex, specialArgumentName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
