package org.blockchainnative.quorum.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.blockchainnative.convert.TypeConverter;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.quorum.metadata.QuorumEventFieldInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.1
 * @author Matthias Veit
 */
public abstract class QuorumEventFieldInfoMixin {

    @JsonCreator
    public QuorumEventFieldInfoMixin(
            @JsonProperty("field") Field field,
            @JsonProperty("sourceFieldName") String fieldName,
            @JsonProperty("sourceFieldIndex")  Optional<Integer> index,
            @JsonProperty("typeConverterClass")  Optional<Class<? extends TypeConverter<?, ?>>> typeConverterClass,
            @JsonProperty("solidityType") AbiDefinition.NamedType solidityType) {

    }
}
