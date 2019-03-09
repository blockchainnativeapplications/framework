package org.blockchainnative.quorum.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.quorum.metadata.QuorumParameterInfo;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.quorum.metadata.QuorumMethodInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.1
 * @author Matthias Veit
 */
public abstract class QuorumMethodInfoMixin {

    @JsonCreator
    public QuorumMethodInfoMixin(@JsonProperty("method") Method method,
                                 @JsonProperty("contractMethodName")String contractMethodName,
                                 @JsonProperty("readOnly") boolean readOnly,
                                 @JsonProperty("specialMethod") boolean specialMethod,
                                 @JsonProperty("parameterInfos") List<QuorumParameterInfo> parameters,
                                 @JsonProperty("abi") AbiDefinition abi,
                                 @JsonProperty("resultTypeConverterClass") Class<? extends TypeConverter<?, ?>> resultTypeConverterClass) {
    }
}
