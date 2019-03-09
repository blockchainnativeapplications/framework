package org.blockchainnative.fabric.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.blockchainnative.convert.TypeConverter;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.fabric.metadata.FabricEventFieldInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public abstract class FabricEventFieldInfoMixin {

    @JsonCreator
    public FabricEventFieldInfoMixin(
            @JsonProperty("field") Field field,
            @JsonProperty("sourceFieldName") String fieldName,
            @JsonProperty("sourceFieldIndex")  Optional<Integer> index,
            @JsonProperty("typeConverterClass")  Optional<Class<? extends TypeConverter<?, ?>>> typeConverterClass) {

    }
}
