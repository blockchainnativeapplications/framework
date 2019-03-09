package org.blockchainnative.quorum.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.blockchainnative.quorum.metadata.QuorumEventInfo;
import org.blockchainnative.quorum.metadata.QuorumMethodInfo;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.quorum.metadata.QuorumContractInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.1
 * @author Matthias Veit
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
public abstract class QuorumContractInfoMixin {

    @JsonCreator
    public QuorumContractInfoMixin(
            @JsonProperty("identifier") String identifier,
            @JsonProperty("contractClass") Class<?> contractClass,
            @JsonProperty("methodInfos") Map<Method, QuorumMethodInfo> methodInfos,
            @JsonProperty("eventInfos") Map<String, QuorumEventInfo> eventInfos,
            @JsonProperty("contractAddress") String contractAddress,
            @JsonProperty("abi") String abi,
            @JsonProperty("binary") String binary,
            @JsonProperty("privateFor") List<String> privateFor) {
    }

    @JsonIgnore
    public abstract AbiDefinition[] getAbiDefinitions();

    @JsonIgnore
    public abstract boolean isDeployed();
}
