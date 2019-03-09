package org.blockchainnative.fabric.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.fabric.metadata.FabricParameterInfo;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Modifies the serialization and deserialization of {@link org.blockchainnative.fabric.metadata.FabricMethodInfo}
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public abstract class FabricMethodInfoMixin {

    @JsonCreator
    public FabricMethodInfoMixin(@JsonProperty("method") Method method,
                                   @JsonProperty("contractMethodName")String contractMethodName,
                                   @JsonProperty("readOnly") boolean readOnly,
                                   @JsonProperty("specialMethod") boolean specialMethod,
                                   @JsonProperty("parameterInfos") List<FabricParameterInfo> parameters,
                                   @JsonProperty("resultTypeConverterClass") Class<? extends TypeConverter<?, ?>> resultTypeConverterClass) {
    }
}
