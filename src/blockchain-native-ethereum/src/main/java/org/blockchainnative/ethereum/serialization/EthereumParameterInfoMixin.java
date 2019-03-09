package org.blockchainnative.ethereum.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.blockchainnative.convert.TypeConverter;

import java.lang.reflect.Parameter;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.ethereum.metadata.EthereumParameterInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public abstract class EthereumParameterInfoMixin {

    @JsonCreator
    public EthereumParameterInfoMixin(@JsonProperty("parameter") Parameter parameter,
                                      @JsonProperty("parameterIndex") int parameterIndex,
                                      @JsonProperty("solidityType") String solidityType,
                                      @JsonProperty("typeConverterClass") Class<? extends TypeConverter<?, ?>> typeConverterClass,
                                      @JsonProperty("passParameterAsType") Class<?> passParameterAsType,
                                      @JsonProperty("specialArgumentName") String specialArgumentName) {
    }

    @JsonIgnore
    public boolean isSpecialArgument(){
        return false;
    }
}
