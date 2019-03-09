package org.blockchainnative.builder;

import org.blockchainnative.annotations.SpecialArgument;
import org.blockchainnative.metadata.ParameterInfo;
import org.blockchainnative.convert.NoOpTypeConverter;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.annotations.ContractParameter;
import org.blockchainnative.util.ReflectionUtil;
import org.blockchainnative.util.StringUtil;

import java.lang.reflect.Parameter;

/**
 * Fluent API to build {@code ParameterInfo} objects.
 *
 * @param <TSelf>              Concrete type of the {@link ParameterInfoBuilder}
 * @param <TParameterInfo>     Concrete type of the {@link ParameterInfo} to be created
 * @param <TMethodInfoBuilder> Concrete type of the {@link MethodInfoBuilder} used by this builder
 * @author Matthias Veit
 * @see org.blockchainnative.metadata.ContractInfo
 * @see ParameterInfo
 * @see MethodInfoBuilder
 * @see ParameterInfoBuilder
 * @see EventInfoBuilder
 * @see EventParameterInfoBuilder
 * @see EventFieldInfoBuilder
 * @since 1.0
 */
public abstract class ParameterInfoBuilder<TSelf extends ParameterInfoBuilder<TSelf, TParameterInfo, TMethodInfoBuilder>,
        TParameterInfo extends ParameterInfo,
        TMethodInfoBuilder extends MethodInfoBuilder> {
    protected final TMethodInfoBuilder methodInfoBuilder;
    protected final Parameter parameter;
    protected final int parameterIndex;
    protected Class<? extends TypeConverter<?, ?>> typeConverterClass;
    protected Class<?> passAsType;
    protected TParameterInfo parameterInfo;
    protected String specialArgName;

    /**
     * Initializes a new {@code ParameterInfoBuilder} for the given parameter and assigns the values
     * from the metadata annotation {@link ContractParameter} and {@link SpecialArgument}
     *
     * @param methodInfoBuilder parent builder
     * @param parameter         smart contract method parameter
     */
    public ParameterInfoBuilder(TMethodInfoBuilder methodInfoBuilder, Parameter parameter) {
        this.methodInfoBuilder = methodInfoBuilder;
        this.parameter = parameter;
        this.parameterIndex = ReflectionUtil.getParameterIndex(this.methodInfoBuilder.method, parameter);
        if (this.parameterIndex == -1) {
            throw new IllegalArgumentException(String.format("Parameter not found on method '%s(...)'.", this.methodInfoBuilder.method.getName()));
        }

        parseAnnotations();
    }

    private void parseAnnotations() {
        // parse values provided via annotation values
        var parameterAnnotation = parameter.getAnnotation(ContractParameter.class);
        if (parameterAnnotation != null) {
            var converterClass = parameterAnnotation.useTypeConverter();
            if (converterClass != null && converterClass != NoOpTypeConverter.class) {
                this.typeConverterClass = converterClass;
            }

            var asClass = parameterAnnotation.asType();
            if (asClass != null && asClass != Void.class) {
                this.passAsType = asClass;
            }
        }
        var specialArgAnnotation = parameter.getAnnotation(SpecialArgument.class);
        if (specialArgAnnotation != null) {
            if (!StringUtil.isNullOrEmpty(specialArgAnnotation.value())) {
                this.specialArgName = specialArgAnnotation.value();
            }
        }
    }

    /**
     * Sets the {@code TypeConverter} to be used to convert the parameter value before passing it to underlying provider. <br>
     * Initial value is taken from {@link ContractParameter#useTypeConverter()}
     *
     * @param typeConverterClass class of the {@code TypeConverter} to be used to convert the parameter
     * @return this {@code ParameterInfoBuilder}
     */
    public TSelf useTypeConverter(Class<? extends TypeConverter<?, ?>> typeConverterClass) {
        this.typeConverterClass = typeConverterClass;
        return self();
    }

    /**
     * Sets the {@code Type} the parameter value should be converted to before passing it to underlying provider. <br>
     * Initial value is taken from {@link ContractParameter#asType()}
     * <p>
     * Due to type erasure you need to use a specific type converter in order to convert to a generic type
     *
     * @param type type the parameter value should be converted to.
     * @return this {@code ParameterInfoBuilder}
     */
    public TSelf passParameterAsType(Class<?> type) {
        this.passAsType = type;
        return self();
    }

    /**
     * Controls whether or not the parameter targeted by this {@code ParameterInfo} is marked as special argument to the underlying provider. <br>
     * How this parameter is interpreted is up to provider. <br>
     * Initial value is taken from {@link SpecialArgument#value()}
     *
     * @param name name of the special argument or null if the parameter should not be treated as special argument
     * @return this {@code ParameterInfoBuilder}
     */
    public TSelf passAsSpecialArgWithName(String name) {
        this.specialArgName = name;
        return self();
    }

    /**
     * Returns whether the parameter targeted by this {@code ParameterInfo} is declared as special argument. <br>
     * That is when the special argument name is not null or empty.
     *
     * @return flag indicating whether or not the parameter targeted by this {@code MethodInfo} is marked as special method
     */
    public boolean isSpecialArgument() {
        return !StringUtil.isNullOrEmpty(specialArgName);
    }

    /**
     * Returns itself as the generic parameter {@code TSelf}. <br>
     * Allows sub types of {@code ParameterInfoBuilder} to return the correct type from the fluent methods like {@link ParameterInfoBuilder#useTypeConverter(Class)}.
     *
     * @return {@code this} casted to {@code TSelf}
     */
    @SuppressWarnings("unchecked")
    protected TSelf self() {
        return (TSelf) this;
    }

    /**
     * Returns if the {@code ParameterInfo} object represented by the builder has already been built or not.
     *
     * @return boolean value indicating if the {@code ParameterInfo} object represented by the builder has already been built or not
     */
    public boolean hasBeenBuilt() {
        return parameterInfo != null;
    }

    /**
     * Returns the {@code ParameterInfo} object declared through the fluent API. <br>
     * Must only be called after {@link ParameterInfoBuilder#build()}.
     *
     * @return {@code ParameterInfo} object represented by the builder
     * @throws IllegalStateException in case the {@code build()} has not been called before.
     */
    public TParameterInfo getParameterInfo() {
        if (this.parameterInfo == null) {
            throw new IllegalStateException("build() must be called before retrieving the ParameterInfo!");
        }
        return this.parameterInfo;
    }

    /**
     * Creates the {@link ParameterInfo} object declared through the fluent API. <br>
     * Must only be called once.
     *
     * @return {@code ParameterInfo} object represented by the builder
     * @throws IllegalStateException in case the {@code build()} has been called before.
     */
    public final TMethodInfoBuilder build() {
        if (this.parameterInfo != null) {
            throw new IllegalStateException("build() must not be called more than once!");
        }
        this.parameterInfo = buildInternal();
        return this.methodInfoBuilder;
    }

    /**
     * Creates the concrete {@code ParameterInfo} represented by the builder.
     *
     * @return {@code ParameterInfo} represented by the builder
     */
    protected abstract TParameterInfo buildInternal();
}
