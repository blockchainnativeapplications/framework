package org.blockchainnative.fabric.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.blockchainnative.fabric.metadata.ChaincodeLanguage;
import org.blockchainnative.fabric.metadata.FabricEventInfo;
import org.blockchainnative.fabric.metadata.FabricMethodInfo;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.fabric.metadata.FabricContractInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.0
 * @author Matthias Veit
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
public abstract class FabricContractInfoMixin {

    @JsonCreator
    public FabricContractInfoMixin(
            @JsonProperty("identifier") String identifier,
            @JsonProperty("contractClass") Class<?> contractClass,
            @JsonProperty("methodInfos") Map<Method, FabricMethodInfo> methodInfos,
            @JsonProperty("eventInfos") Map<String, FabricEventInfo> eventInfos,
            @JsonProperty("chaincodeID") ChaincodeID chaincodeID,
            @JsonProperty("chaincodeLanguage") ChaincodeLanguage chaincodeLanguage,
            @JsonProperty("chaincodePolicy") ChaincodeEndorsementPolicy chaincodePolicy,
            @JsonProperty("chaincodeSourceDirectory") String chaincodeSourceDirectory,
            @JsonProperty("targetPeerNames") Set<String> targetPeerNames) {
    }

    @JsonIgnore
    public abstract boolean isInstalled();

    @JsonIgnore
    public abstract boolean isInstantiated();

}
