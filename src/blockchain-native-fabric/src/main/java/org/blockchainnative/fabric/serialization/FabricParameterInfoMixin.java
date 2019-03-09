package org.blockchainnative.fabric.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.blockchainnative.convert.TypeConverter;

import java.lang.reflect.Parameter;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.fabric.metadata.FabricParameterInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public abstract class FabricParameterInfoMixin {

    @JsonCreator
    public FabricParameterInfoMixin(@JsonProperty("parameter") Parameter parameter,
                                    @JsonProperty("parameterIndex") int parameterIndex,
                                    @JsonProperty("typeConverterClass") Class<? extends TypeConverter<?, ?>> typeConverterClass,
                                    @JsonProperty("passParameterAsType") Class<?> passParameterAsType,
                                    @JsonProperty("specialArgumentName") String specialArgumentName) {
    }

    @JsonIgnore
    public boolean isSpecialArgument(){
        return false;
    }
}
