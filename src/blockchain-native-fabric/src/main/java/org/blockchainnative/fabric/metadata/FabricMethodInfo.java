package org.blockchainnative.fabric.metadata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.metadata.MethodInfo;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Holds additional information about a smart contract method. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.fabric.builder.FabricContractInfoBuilder} to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.fabric.builder.FabricContractInfoBuilder
 * @since 1.0
 */
public class FabricMethodInfo extends MethodInfo<FabricParameterInfo> {

    /**
     * Constructs a new {@code FabricMethodInfo}
     *
     * @param method                   method on the contract interface representing the smart contract method.
     * @param contractMethodName       name of the corresponding smart contract method.
     * @param readOnly                 specifies whether the method is marked as readonly
     * @param specialMethod            specifies whether the method is marked as special method
     * @param parameters               {@code FabricParameterInfo} objects
     */
    public FabricMethodInfo(Method method, String contractMethodName, boolean readOnly, boolean specialMethod, List<FabricParameterInfo> parameters) {
        super(method, contractMethodName, readOnly, specialMethod, parameters);
    }

    /**
     * Constructs a new {@code FabricMethodInfo}
     *
     * @param method                   method on the contract interface representing the smart contract method.
     * @param contractMethodName       name of the corresponding smart contract method.
     * @param readOnly                 specifies whether the method is marked as readonly
     * @param specialMethod            specifies whether the method is marked as special method
     * @param parameters               {@code FabricParameterInfo} objects
     * @param resultTypeConverterClass type converter used to convert the smart contract method result value to the
     *                                 declared type of the wrapper method
     */
    public FabricMethodInfo(Method method, String contractMethodName, boolean readOnly, boolean specialMethod, List<FabricParameterInfo> parameters, Class<? extends TypeConverter<?, ?>> resultTypeConverterClass) {
        super(method, contractMethodName, readOnly, specialMethod, parameters, resultTypeConverterClass);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
