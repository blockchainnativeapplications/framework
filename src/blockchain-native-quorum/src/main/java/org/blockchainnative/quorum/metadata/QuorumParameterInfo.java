package org.blockchainnative.quorum.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.metadata.ParameterInfo;

import java.lang.reflect.Parameter;

/**
 * Holds additional information about a parameter of a smart contract method. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.quorum.builder.QuorumContractInfoBuilder} to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.quorum.QuorumContractWrapperGenerator
 * @since 1.1
 */
public class QuorumParameterInfo extends ParameterInfo {
    private final String solidityType;

    /**
     * Constructs a new {@code QuorumParameterInfo}
     *
     * @param parameter      parameter on the method on the smart contract interface.
     * @param parameterIndex index of the parameter as it occurs in the method declaration
     * @param solidityType   type of the parameter as defined in the ABI
     */
    public QuorumParameterInfo(Parameter parameter, int parameterIndex, String solidityType) {
        this(parameter, parameterIndex, solidityType, null, null, null);
    }

    /**
     * Constructs a new {@code QuorumParameterInfo}
     *
     * @param parameter           parameter on the method on the smart contract interface.
     * @param parameterIndex      index of the parameter as it occurs in the method declaration
     * @param solidityType        type of the parameter as defined in the ABI
     * @param typeConverterClass  type converter used to convert the parameter before passing it to the underlying
     *                            provider
     * @param passParameterAsType type the parameter should be converted to before passing it to the underlying
     *                            provider
     * @param specialArgumentName special argument name for this parameter
     */
    public QuorumParameterInfo(Parameter parameter, int parameterIndex, String solidityType, Class<? extends TypeConverter<?, ?>> typeConverterClass, Class<?> passParameterAsType, String specialArgumentName) {
        super(parameter, parameterIndex, typeConverterClass, passParameterAsType, specialArgumentName);
        this.solidityType = solidityType;
    }

    /**
     * Returns the parameter's solidity type
     *
     * @return solidity type of the parameter
     */
    public String getSolidityType() {
        return solidityType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof QuorumParameterInfo)) return false;

        QuorumParameterInfo that = (QuorumParameterInfo) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(solidityType, that.solidityType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(solidityType)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("solidityType", solidityType)
                .toString();
    }
}
