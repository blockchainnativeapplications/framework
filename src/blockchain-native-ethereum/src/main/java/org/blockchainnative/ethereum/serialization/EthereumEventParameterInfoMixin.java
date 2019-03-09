package org.blockchainnative.ethereum.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Parameter;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.ethereum.metadata.EthereumEventParameterInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public abstract class EthereumEventParameterInfoMixin {

    @JsonCreator
    public EthereumEventParameterInfoMixin(
            @JsonProperty("parameter") Parameter parameter,
            @JsonProperty("parameterIndex") int parameterIndex,
            @JsonProperty("specialArgumentName") String specialArgumentName) {
    }

    @JsonIgnore
    public boolean isSpecialArgument(){
        return false;
    }
}
