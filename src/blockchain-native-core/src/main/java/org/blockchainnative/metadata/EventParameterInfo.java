package org.blockchainnative.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.util.StringUtil;

import java.lang.reflect.Parameter;

/**
 * Holds additional information about a parameter of a smart contract event method. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.builder.ContractInfoBuilder} and its subtypes to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.builder.ContractInfoBuilder
 * @see ContractInfo
 * @see org.blockchainnative.ContractWrapperGenerator
 * @since 1.0
 */
public class EventParameterInfo {
    protected final Parameter parameter;
    protected final int parameterIndex;
    protected final String specialArgumentName;

    /**
     * Constructs a new {@code EventParameterInfo}.
     *
     * @param parameter           parameter on the method on the smart contract interface representing the event.
     * @param parameterIndex      index of the parameter as it occurs in the method declaration
     * @param specialArgumentName special argument name for this parameter
     */
    public EventParameterInfo(Parameter parameter, int parameterIndex, String specialArgumentName) {
        this.parameter = parameter;
        this.parameterIndex = parameterIndex;
        this.specialArgumentName = specialArgumentName;
    }

    /**
     * Returns the parameter of the method of the corresponding contract interface.
     *
     * @return parameter of the method of the corresponding contract interface.
     */
    public Parameter getParameter() {
        return parameter;
    }

    /**
     * Returns the index of the parameter on the method of the corresponding contract interface.
     *
     * @return index of the parameter on the method of the corresponding contract interface.
     */
    public int getParameterIndex() {
        return parameterIndex;
    }

    /**
     * Returns the special argument name defined for this parameter.
     *
     * @return special argument name defined for this parameter.
     */
    public String getSpecialArgumentName() {
        return specialArgumentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof EventParameterInfo)) return false;

        EventParameterInfo that = (EventParameterInfo) o;

        return new EqualsBuilder()
                .append(parameterIndex, that.parameterIndex)
                .append(parameter, that.parameter)
                .append(specialArgumentName, that.specialArgumentName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(parameter)
                .append(parameterIndex)
                .append(specialArgumentName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("parameter", parameter)
                .append("parameterIndex", parameterIndex)
                .append("specialArgumentName", specialArgumentName)
                .toString();
    }
}
