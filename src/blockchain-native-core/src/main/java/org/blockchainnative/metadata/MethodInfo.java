package org.blockchainnative.metadata;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Holds additional information about a smart contract method. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.builder.ContractInfoBuilder} and its subtypes to construct
 * instances of this class.
 *
 * @param <TParameterInfo> Concrete type of the {@link ParameterInfo} objects used by {@code MethodInfo}
 * @author Matthias Veit
 * @see org.blockchainnative.builder.ContractInfoBuilder
 * @see ContractInfo
 * @see org.blockchainnative.ContractWrapperGenerator
 * @since 1.0
 */
public class MethodInfo<TParameterInfo extends ParameterInfo> {
    private final Method method;
    private final String contractMethodName;
    private final boolean readOnly;
    private final boolean specialMethod;
    private final boolean async;
    private final boolean voidReturnType;
    private final Optional<Class<? extends TypeConverter<?, ?>>> resultTypeConverterClass;
    private final List<TParameterInfo> parameters;


    /**
     * Constructs a new {@code MethodInfo}
     *
     * @param method             method on the contract interface representing the smart contract method.
     * @param contractMethodName name of the corresponding smart contract method.
     * @param readOnly           specifies whether the method is marked as readonly
     * @param specialMethod      specifies whether the method is marked as special method
     * @param parameters         {@code ParameterInfo} objects
     */
    public MethodInfo(Method method, String contractMethodName, boolean readOnly, boolean specialMethod, List<TParameterInfo> parameters) {
        this(method, contractMethodName, readOnly, specialMethod, parameters, null);
    }

    /**
     * Constructs a new {@code MethodInfo}
     *
     * @param method                   method on the contract interface representing the smart contract method.
     * @param contractMethodName       name of the corresponding smart contract method.
     * @param readOnly                 specifies whether the method is marked as readonly
     * @param specialMethod            specifies whether the method is marked as special method
     * @param parameters               {@code ParameterInfo} objects
     * @param resultTypeConverterClass type converter used to convert the smart contract method result value to the declared type of the wrapper method
     */
    public MethodInfo(Method method, String contractMethodName, boolean readOnly, boolean specialMethod, List<TParameterInfo> parameters, Class<? extends TypeConverter<?, ?>> resultTypeConverterClass) {
        this.method = method;
        this.contractMethodName = contractMethodName != null ? contractMethodName : method.getName();
        this.readOnly = readOnly;
        this.specialMethod = specialMethod;
        this.async = ReflectionUtil.isAsyncReturnType(method);
        this.voidReturnType = ReflectionUtil.isVoidMethod(method);
        this.resultTypeConverterClass = Optional.ofNullable(resultTypeConverterClass);

        this.parameters = new ArrayList<>();
        if (parameters != null) {
            this.parameters.addAll(parameters);
        }
    }

    /**
     * Returns method of the corresponding contract interface.
     *
     * @return method of the corresponding contract interface.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns name of the targeted smart contract method.
     *
     * @return name of the targeted smart contract method.
     */
    public String getContractMethodName() {
        return contractMethodName;
    }

    /**
     * Returns the class of the type converter to be used to convert the smart contract result to the declared return type
     * of the {@code MethodInfo}'s method or an empty {@code Optional}.
     *
     * @return class of the type converter or an empty {@code Optional}
     */
    public Optional<Class<? extends TypeConverter<?, ?>>> getResultTypeConverterClass() {
        return resultTypeConverterClass;
    }

    /**
     * Returns all {@code ParameterInfo} objects registered with this {@code MethodInfo}.
     *
     * @return List of all {@code ParameterInfo} objects registered with this {@code MethodInfo}.
     */
    public List<TParameterInfo> getParameterInfos() {
        return new ArrayList<>(parameters);
    }

    /**
     * Returns whether or not the method is marked as readonly, i.e. not modifying the blockchain state.
     *
     * @return flag indicating whether or not the method is marked as readonly
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Returns whether or not the method is marked as async, i.e. declaring {@link java.util.concurrent.Future}
     * or {@link java.util.concurrent.CompletableFuture} as return type.
     *
     * @return flag indicating whether or not the method is marked as async.
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Returns whether or not the method's return type is void.
     *
     * @return flag indicating whether or not the method's return type is void.
     */
    public boolean isVoidReturnType() {
        return voidReturnType;
    }

    /**
     * Returns whether or not the method is marked as special method.
     *
     * @return flag indicating whether or not the method is marked as special method.
     */
    public boolean isSpecialMethod() {
        return specialMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof MethodInfo)) return false;

        MethodInfo<?> that = (MethodInfo<?>) o;

        return new EqualsBuilder()
                .append(readOnly, that.readOnly)
                .append(specialMethod, that.specialMethod)
                .append(async, that.async)
                .append(voidReturnType, that.voidReturnType)
                .append(method, that.method)
                .append(contractMethodName, that.contractMethodName)
                .append(resultTypeConverterClass, that.resultTypeConverterClass)
                .append(parameters, that.parameters)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(method)
                .append(contractMethodName)
                .append(readOnly)
                .append(specialMethod)
                .append(async)
                .append(voidReturnType)
                .append(resultTypeConverterClass)
                .append(parameters)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("method", method)
                .append("contractMethodName", contractMethodName)
                .append("readOnly", readOnly)
                .append("specialMethod", specialMethod)
                .append("async", async)
                .append("voidReturnType", voidReturnType)
                .append("resultTypeConverterClass", resultTypeConverterClass)
                .toString();
    }
}
