package org.blockchainnative.quorum.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.metadata.MethodInfo;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Holds additional information about a smart contract method. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.quorum.builder.QuorumContractInfoBuilder} to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.quorum.QuorumContractWrapperGenerator
 * @since 1.1
 */
public class QuorumMethodInfo extends MethodInfo<QuorumParameterInfo> {
    private AbiDefinition abi;

    /**
     * Constructs a new {@code QuorumMethodInfo}
     *
     * @param method                   method on the contract interface representing the smart contract method.
     * @param contractMethodName       name of the corresponding smart contract method as defined in the ABI.
     * @param readOnly                 specifies whether the method is marked as readonly
     * @param specialMethod            specifies whether the method is marked as special method
     * @param parameters               {@code QuorumParameterInfo} objects
     * @param abi                      ABI definition of the corresponding smart contract method
     */
    public QuorumMethodInfo(Method method, String contractMethodName, boolean readOnly, boolean specialMethod, List<QuorumParameterInfo> parameters, AbiDefinition abi) {
        this(method, contractMethodName, readOnly, specialMethod, parameters, abi, null);
    }

    /**
     * Constructs a new {@code QuorumMethodInfo}
     *
     * @param method                   method on the contract interface representing the smart contract method.
     * @param contractMethodName       name of the corresponding smart contract method as defined in the ABI.
     * @param readOnly                 specifies whether the method is marked as readonly
     * @param specialMethod            specifies whether the method is marked as special method
     * @param parameters               {@code QuorumParameterInfo} objects
     * @param abi                      ABI definition of the corresponding smart contract method
     * @param resultTypeConverterClass type converter used to convert the smart contract method result value to the
     *                                 declared type of the wrapper method
     */
    public QuorumMethodInfo(Method method, String contractMethodName, boolean readOnly, boolean specialMethod, List<QuorumParameterInfo> parameters, AbiDefinition abi, Class<? extends TypeConverter<?, ?>> resultTypeConverterClass) {
        super(method, contractMethodName, readOnly, specialMethod, parameters, resultTypeConverterClass);
        this.abi = abi;
    }

    /**
     * Returns the ABI definition of the corresponding smart contract method
     *
     * @return ABI definition of the corresponding smart contract method
     */
    public AbiDefinition getAbi() {
        return abi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof QuorumMethodInfo)) return false;

        QuorumMethodInfo that = (QuorumMethodInfo) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(abi, that.abi)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(abi)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
