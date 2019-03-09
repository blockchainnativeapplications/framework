package org.blockchainnative.ethereum.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.blockchainnative.ethereum.metadata.EthereumEventInfo;
import org.blockchainnative.ethereum.metadata.EthereumMethodInfo;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.ethereum.metadata.EthereumContractInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.0
 * @author Matthias Veit
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
public abstract class EthereumContractInfoMixin  {

    @JsonCreator
    public EthereumContractInfoMixin(
            @JsonProperty("identifier") String identifier,
            @JsonProperty("contractClass") Class<?> contractClass,
            @JsonProperty("methodInfos") Map<Method, EthereumMethodInfo> methodInfos,
            @JsonProperty("eventInfos") Map<String, EthereumEventInfo> eventInfos,
            @JsonProperty("contractAddress") String contractAddress,
            @JsonProperty("abi") String abi,
            @JsonProperty("binary") String binary) {
    }

    @JsonIgnore
    public abstract AbiDefinition[] getAbiDefinitions();

    @JsonIgnore
    public abstract boolean isDeployed();
}
