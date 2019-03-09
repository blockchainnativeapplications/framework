package org.blockchainnative.fabric.metadata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.metadata.EventFieldInfo;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Holds additional information about a field of a class which is used as smart contract event type. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.fabric.builder.FabricContractInfoBuilder} to construct instances
 * of this class.
 * <p>
 * Since Hyperledger Fabric chaincode events only contain a single payload, only one {@code FabricEventFieldInfo} is
 * supported per {@code FabricEventInfo}.
 * </p>
 *
 * @author Matthias Veit
 * @see org.blockchainnative.fabric.builder.FabricContractInfoBuilder
 * @since 1.0
 */
public class FabricEventFieldInfo extends EventFieldInfo {

    /**
     * Constructs a new {@code FabricEventFieldInfo}
     *
     * @param field              field targeted by this {@code FabricEventFieldInfo} on the event type
     * @param typeConverterClass type converter used to convert the smart contract event field value to the declared
     *                           type of this field
     */
    public FabricEventFieldInfo(Field field, Optional<Class<? extends TypeConverter<?, ?>>> typeConverterClass) {
        super(field, "payload", Optional.empty(), typeConverterClass);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
