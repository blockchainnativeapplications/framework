package org.blockchainnative.fabric.metadata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.metadata.ParameterInfo;

import java.lang.reflect.Parameter;

/**
 * Holds additional information about a parameter of a smart contract method. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.fabric.builder.FabricContractInfoBuilder} to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.fabric.builder.FabricContractInfoBuilder
 * @since 1.0
 */
public class FabricParameterInfo extends ParameterInfo {

    /**
     * Constructs a new {@code ParameterInfo}
     *
     * @param parameter      parameter on the method on the smart contract interface.
     * @param parameterIndex index of the parameter as it occurs in the method declaration
     */
    public FabricParameterInfo(Parameter parameter, int parameterIndex) {
        super(parameter, parameterIndex);
    }

    /**
     * Constructs a new {@code EthereumParameterInfo}
     *
     * @param parameter           parameter on the method on the smart contract interface.
     * @param parameterIndex      index of the parameter as it occurs in the method declaration
     * @param typeConverterClass  type converter used to convert the parameter before passing it to the underlying
     *                            provider
     * @param passParameterAsType type the parameter should be converted to before passing it to the underlying
     *                            provider
     * @param specialArgumentName special argument name for this parameter
     */
    public FabricParameterInfo(Parameter parameter, int parameterIndex, Class<? extends TypeConverter<?, ?>> typeConverterClass, Class<?> passParameterAsType, String specialArgumentName) {
        super(parameter, parameterIndex, typeConverterClass, passParameterAsType, specialArgumentName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }


}

