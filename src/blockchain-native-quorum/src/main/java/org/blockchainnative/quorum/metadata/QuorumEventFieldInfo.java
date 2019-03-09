package org.blockchainnative.quorum.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.metadata.EventFieldInfo;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Holds additional information about a field of a class which is used as smart contract event type. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.quorum.builder.QuorumContractInfoBuilder} to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.quorum.QuorumContractWrapperGenerator
 * @since 1.1
 */
public class QuorumEventFieldInfo extends EventFieldInfo {
    public final AbiDefinition.NamedType solidityType;

    /**
     * Constructs a new {@code QuorumEventFieldInfo}
     *
     * @param field              field targeted by this {@code QuorumEventFieldInfo} on the event type
     * @param fieldName          name of corresponding field as defined in the ABI
     * @param index              index of corresponding field as defined in the ABI
     * @param typeConverterClass type converter used to convert the smart contract event field value to the declared
     *                           type of this field
     * @param solidityType       type of field as defined in the ABI
     */
    public QuorumEventFieldInfo(Field field, String fieldName, Optional<Integer> index, Optional<Class<? extends TypeConverter<?, ?>>> typeConverterClass, AbiDefinition.NamedType solidityType) {
        super(field, fieldName, index, typeConverterClass);
        this.solidityType = solidityType;
    }

    /**
     * Returns the type of the field as defined in the ABI.
     *
     * @return type of the field as defined in the ABI.
     */
    public AbiDefinition.NamedType getSolidityType() {
        return solidityType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof QuorumEventFieldInfo)) return false;

        QuorumEventFieldInfo that = (QuorumEventFieldInfo) o;

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
                .append("solidityType", solidityType != null ? solidityType.getType() : null)
                .toString();
    }
}
