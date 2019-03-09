package org.blockchainnative.ethereum.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.blockchainnative.convert.TypeConverter;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.ethereum.metadata.EthereumEventFieldInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public abstract class EthereumEventFieldInfoMixin {

    @JsonCreator
    public EthereumEventFieldInfoMixin(
            @JsonProperty("field") Field field,
            @JsonProperty("sourceFieldName") String fieldName,
            @JsonProperty("sourceFieldIndex")  Optional<Integer> index,
            @JsonProperty("typeConverterClass")  Optional<Class<? extends TypeConverter<?, ?>>> typeConverterClass,
            @JsonProperty("solidityType") AbiDefinition.NamedType solidityType) {

    }
}
