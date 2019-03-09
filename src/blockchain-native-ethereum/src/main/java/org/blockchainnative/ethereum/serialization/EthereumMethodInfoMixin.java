package org.blockchainnative.ethereum.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.ethereum.metadata.EthereumParameterInfo;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.ethereum.metadata.EthereumMethodInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public abstract class EthereumMethodInfoMixin {

    @JsonCreator
    public EthereumMethodInfoMixin(@JsonProperty("method") Method method,
                                   @JsonProperty("contractMethodName")String contractMethodName,
                                   @JsonProperty("readOnly") boolean readOnly,
                                   @JsonProperty("specialMethod") boolean specialMethod,
                                   @JsonProperty("parameterInfos") List<EthereumParameterInfo> parameters,
                                   @JsonProperty("abi") AbiDefinition abi,
                                   @JsonProperty("resultTypeConverterClass") Class<? extends TypeConverter<?, ?>> resultTypeConverterClass) {
    }
}
