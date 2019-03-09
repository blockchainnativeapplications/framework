package org.blockchainnative.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.util.StringUtil;

import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * Holds additional information about a parameter of a smart contract method. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.builder.ContractInfoBuilder} and its subtypes to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.builder.ContractInfoBuilder
 * @see ContractInfo
 * @see org.blockchainnative.ContractWrapperGenerator
 * @since 1.0
 */
public class ParameterInfo {

    private final Parameter parameter;
    private final int parameterIndex;
    private final Optional<Class<? extends TypeConverter<?, ?>>> typeConverterClass;
    private final Optional<Class<?>> passParameterAsType;
    private final String specialArgumentName;

    /**
     * Constructs a new {@code ParameterInfo}
     *
     * @param parameter      parameter on the method on the smart contract interface.
     * @param parameterIndex index of the parameter as it occurs in the method declaration
     */
    public ParameterInfo(Parameter parameter, int parameterIndex) {
        this(parameter, parameterIndex, null, null, null);
    }

    /**
     * Constructs a new {@code ParameterInfo}
     *
     * @param parameter           parameter on the method on the smart contract interface.
     * @param parameterIndex      index of the parameter as it occurs in the method declaration
     * @param typeConverterClass  type converter used to convert the parameter before passing it to the underlying provider
     * @param passParameterAsType type the parameter should be converted to before passing it to the underlying provider
     * @param specialArgumentName special argument name for this parameter
     */
    public ParameterInfo(Parameter parameter, int parameterIndex, Class<? extends TypeConverter<?, ?>> typeConverterClass, Class<?> passParameterAsType, String specialArgumentName) {
        this.parameter = parameter;
        this.parameterIndex = parameterIndex;
        this.specialArgumentName = specialArgumentName;
        this.typeConverterClass = Optional.ofNullable(typeConverterClass);
        this.passParameterAsType = Optional.ofNullable(passParameterAsType);
    }

    /**
     * Returns the parameter of the method of the corresponding contract interface.
     *
     * @return parameter of the method of the corresponding contract interface.
     */
    public Parameter getParameter() {
        return parameter;
    }

    /**
     * Returns the index of the parameter on the method of the corresponding contract interface.
     *
     * @return index of the parameter on the method of the corresponding contract interface.
     */
    public int getParameterIndex() {
        return parameterIndex;
    }

    /**
     * Gets the {@code TypeConverter} to be used to convert the parameter value before passing it to underlying blockchain provider.
     *
     * @return {@code TypeConverter} to be used to convert the parameter value.
     */
    public Optional<Class<? extends TypeConverter<?, ?>>> getTypeConverterClass() {
        return typeConverterClass;
    }

    /**
     * Gets the {@code Type} the parameter value should be converted to before passing it to underlying blockchain provider.
     *
     * @return {@code Type} the parameter value should be converted to.
     */
    public Optional<Class<?>> getPassParameterAsType() {
        return passParameterAsType;
    }

    /**
     * Returns the special argument name defined for this parameter.
     *
     * @return special argument name defined for this parameter.
     */
    public String getSpecialArgumentName() {
        return specialArgumentName;
    }

    /**
     * Returns whether or no the parameter is marked as special argument, i.e. its special argument name is set or not.
     *
     * @return flag indicating whether or no the parameter is marked as special argument.
     */
    public boolean isSpecialArgument() {
        return !StringUtil.isNullOrEmpty(specialArgumentName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ParameterInfo)) return false;

        ParameterInfo that = (ParameterInfo) o;

        return new EqualsBuilder()
                .append(parameterIndex, that.parameterIndex)
                .append(parameter, that.parameter)
                .append(typeConverterClass, that.typeConverterClass)
                .append(passParameterAsType, that.passParameterAsType)
                .append(specialArgumentName, that.specialArgumentName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(parameter)
                .append(parameterIndex)
                .append(typeConverterClass)
                .append(passParameterAsType)
                .append(specialArgumentName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("field", parameter)
                .append("parameterIndex", parameterIndex)
                .append("typeConverterClass", typeConverterClass)
                .append("passParameterAsType", passParameterAsType)
                .append("specialArgumentName", specialArgumentName)
                .toString();
    }
}
