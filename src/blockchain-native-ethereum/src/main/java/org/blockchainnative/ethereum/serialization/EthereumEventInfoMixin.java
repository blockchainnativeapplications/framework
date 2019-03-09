package org.blockchainnative.ethereum.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.blockchainnative.ethereum.metadata.EthereumEventFieldInfo;
import org.blockchainnative.ethereum.metadata.EthereumEventParameterInfo;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.ethereum.metadata.EthereumEventInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public abstract class EthereumEventInfoMixin {

    @JsonCreator
    public EthereumEventInfoMixin(@JsonProperty("eventName") String name,
                                  @JsonProperty("method") Method method,
                                  @JsonProperty("eventParameterInfos") Collection<EthereumEventParameterInfo> ethereumEventParameterInfos,
                                  @JsonProperty("eventFieldInfos") Collection<EthereumEventFieldInfo> ethereumEventFieldInfos,
                                  @JsonProperty("abiDefinition") AbiDefinition abiDefinition) {

    }

    @JsonIgnore
    public Class<?> getEventType() {
        return null;
    }
}
