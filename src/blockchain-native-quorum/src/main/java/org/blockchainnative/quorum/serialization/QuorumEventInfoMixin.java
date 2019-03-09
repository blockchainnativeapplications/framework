package org.blockchainnative.quorum.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.blockchainnative.quorum.metadata.QuorumEventFieldInfo;
import org.blockchainnative.quorum.metadata.QuorumEventParameterInfo;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.quorum.metadata.QuorumEventInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.1
 * @author Matthias Veit
 */
public abstract class QuorumEventInfoMixin {

    @JsonCreator
    public QuorumEventInfoMixin(@JsonProperty("eventName") String name,
                                @JsonProperty("method") Method method,
                                @JsonProperty("eventParameterInfos") Collection<QuorumEventParameterInfo> eventParameterInfos,
                                @JsonProperty("eventFieldInfos") Collection<QuorumEventFieldInfo> eventFieldInfos,
                                @JsonProperty("abiDefinition") AbiDefinition abiDefinition) {

    }

    @JsonIgnore
    public Class<?> getEventType() {
        return null;
    }
}
