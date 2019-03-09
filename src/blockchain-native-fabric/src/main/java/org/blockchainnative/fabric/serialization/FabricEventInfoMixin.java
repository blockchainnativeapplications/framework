package org.blockchainnative.fabric.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.blockchainnative.fabric.metadata.FabricEventFieldInfo;
import org.blockchainnative.fabric.metadata.FabricEventParameterInfo;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.fabric.metadata.FabricEventInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public abstract class FabricEventInfoMixin {

    @JsonCreator
    public FabricEventInfoMixin(@JsonProperty("eventName") String name,
                                @JsonProperty("method") Method method,
                                @JsonProperty("eventParameterInfos") Collection<FabricEventParameterInfo> eventParameterInfos,
                                @JsonProperty("eventFieldInfos") Collection<FabricEventFieldInfo> eventFieldInfos) {

    }

    @JsonIgnore
    public Class<?> getEventType() {
        return null;
    }
}
