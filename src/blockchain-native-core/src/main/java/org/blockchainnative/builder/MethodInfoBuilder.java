package org.blockchainnative.builder;

import org.blockchainnative.convert.NoOpTypeConverter;
import org.blockchainnative.metadata.MethodInfo;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.util.ReflectionUtil;
import org.blockchainnative.util.StringUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Fluent API to build {@code MethodInfo} objects.
 *
 * @param <TSelf>                 Concrete type of the {@link MethodInfoBuilder}
 * @param <TMethodInfo>           Concrete type of the {@link MethodInfo} to be created
 * @param <TContractInfoBuilder>  Concrete type of the {@link ContractInfoBuilder} used by this builder
 * @param <TParameterInfoBuilder> Concrete type of the {@link ParameterInfoBuilder} used by this builder
 * @author Matthias Veit
 * @see org.blockchainnative.metadata.ContractInfo
 * @see MethodInfo
 * @see MethodInfoBuilder
 * @see ParameterInfoBuilder
 * @see EventInfoBuilder
 * @see EventParameterInfoBuilder
 * @see EventFieldInfoBuilder
 * @since 1.0
 */
public abstract class MethodInfoBuilder<TSelf extends MethodInfoBuilder<TSelf, TMethodInfo, TContractInfoBuilder, TParameterInfoBuilder>,
        TMethodInfo extends MethodInfo,
        TContractInfoBuilder extends ContractInfoBuilder,
        TParameterInfoBuilder extends ParameterInfoBuilder> {

    protected final TContractInfoBuilder contractInfoBuilder;
    protected final Method method;
    protected final SortedMap<Parameter, TParameterInfoBuilder> parameterInfoBuilders;
    protected String contractMethodName;
    protected boolean isReadOnly;
    protected boolean isSpecialMethod;
    protected Class<? extends TypeConverter<?, ?>> resultTypeConverterClass;
    protected TMethodInfo methodInfo;

    /**
     * Initializes a new {@code MethodInfoBuilder} for the given method and assigns the values from the metadata
     * annotation {@link ContractMethod}
     *
     * @param contractInfoBuilder parent builder
     * @param method              smart contract method
     */
    protected MethodInfoBuilder(TContractInfoBuilder contractInfoBuilder, Method method) {
        if (contractInfoBuilder == null) throw new IllegalArgumentException("ContractInfoBuilder must not be null!");
        if (method == null) throw new IllegalArgumentException("Method must not be null!");

        this.contractInfoBuilder = contractInfoBuilder;
        this.method = method;
        this.parameterInfoBuilders = new TreeMap<>(new ParameterByIndexComparator(method));

        // pre-populate ParameterBuilders
        for (var parameter : method.getParameters()) {
            this.parameterInfoBuilders.put(parameter, this.builderForParameterInternal(parameter));
        }

        parseAnnotations();
    }

    private void parseAnnotations() {
        // parse values provided via annotation values
        var methodAnnotation = method.getAnnotation(ContractMethod.class);
        if (methodAnnotation != null) {
            this.isReadOnly = methodAnnotation.isReadOnly();
            this.isSpecialMethod = methodAnnotation.isSpecialMethod();

            var converterClass = methodAnnotation.useTypeConverter();
            if (converterClass != null && converterClass != NoOpTypeConverter.class) {
                this.resultTypeConverterClass = converterClass;
            }
        }

        if (methodAnnotation != null && !StringUtil.isNullOrEmpty(methodAnnotation.value())) {
            this.contractMethodName = methodAnnotation.value();
        } else {
            this.contractMethodName = this.method.getName();
        }
    }


    /**
     * Sets the name of the smart contract method targeted by this {@code MethodInfo}. <br> Initial value is taken from
     * {@link ContractMethod#value()}
     *
     * @param name name of the smart contract method targeted by this {@code MethodInfo}
     * @return {@code MethodInfoBuilder}
     */
    public TSelf name(String name) {
        this.contractMethodName = name;
        return self();
    }

    /**
     * Controls whether or not the smart contract method targeted by this {@code MethodInfo} should be called without
     * modifying the blockchain state. <br> Initial value is taken from {@link ContractMethod#isReadOnly()}
     *
     * @param isReadOnly flag indicating whether or not the smart contract method targeted by this {@code MethodInfo}
     *                   should be called without modifying the blockchain state.
     * @return {@code MethodInfoBuilder}
     */
    public TSelf readonly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
        return self();
    }

    /**
     * Sets the {@code TypeConverter} to be used to convert the smart contracts result to the method's declared return
     * type. <br> Initial value is taken from {@link ContractMethod#useTypeConverter()}
     *
     * @param typeConverterClass class of the {@code TypeConverter} to be used to convert the smart contract's result
     * @return {@code MethodInfoBuilder}
     */
    public TSelf useResultTypeConverter(Class<? extends TypeConverter<?, ?>> typeConverterClass) {
        this.resultTypeConverterClass = typeConverterClass;
        return self();
    }

    /**
     * Controls whether or not the method targeted by this {@code MethodInfo} is marked as special method to the
     * underlying provider. <br> How this method is interpreted is up to provider. <br> Initial value is taken from
     * {@link ContractMethod#isSpecialMethod()}
     *
     * @param isSpecialMethod flag indicating whether or not the method targeted by this {@code MethodInfo} is marked as
     *                        special method
     * @return {@code MethodInfoBuilder}
     */
    public TSelf specialMethod(boolean isSpecialMethod) {
        this.isSpecialMethod = isSpecialMethod;
        return self();
    }

    /**
     * Returns itself as the generic parameter {@code TSelf}. <br> Allows sub types of {@code MethodInfoBuilder} to
     * return the correct type from the fluent methods like {@link MethodInfoBuilder#name(String)}.
     *
     * @return {@code this} casted to {@code TSelf}
     */
    @SuppressWarnings("unchecked")
    protected TSelf self() {
        return (TSelf) this;
    }


    /**
     * Creates or returns a preexisting {@link ParameterInfoBuilder} for a parameter of the contract method targeted by
     * this {@code MethodInfo}
     *
     * @param parameter parameter of the method targeted by this {@code MethodInfo}
     * @return {@link ParameterInfoBuilder} for the specified parameter
     * @throws IllegalArgumentException in case the parameter could not be found on the targeted method.
     */
    public TParameterInfoBuilder parameter(Parameter parameter) {
        if (parameter == null) throw new IllegalArgumentException("parameter must not be null");

        var parameters = method.getParameters();
        var parameterIndex = -1;
        for (var i = 0; i < parameters.length; i++) {
            if (parameters[i].equals(parameter)) {
                parameterIndex = i;
                break;
            }
        }

        if (parameterIndex == -1) {
            throw new IllegalArgumentException(
                    String.format("Could not find parameter '%s <%s>' on method '%s(...)' of type '%s'",
                            parameter.getName(),
                            parameter.getType().getSimpleName(),
                            method.getName(),
                            contractInfoBuilder.contractType.getTypeName()));
        }

        return getOrCreateParameterInfoBuilder(parameter);
    }

    /**
     * Creates or returns a preexisting {@link ParameterInfoBuilder} for a parameter of the contract method targeted by
     * this {@code MethodInfo}
     *
     * @param i index of a parameter of the method targeted by this {@code MethodInfo}
     * @return {@link ParameterInfoBuilder} for the specified parameter
     * @throws IllegalArgumentException in case the parameter could not be found on the targeted method.
     */
    public TParameterInfoBuilder parameter(int i) {
        if (i < 0)
            throw new IllegalArgumentException("Parameter index needs to be greater than zero");

        if (i >= method.getParameters().length)
            throw new IllegalArgumentException(
                    String.format("Could not provide builder for field at position %s on method '%s(...)' of type '%s since it only takes %s parameters.'",
                            i,
                            method.getName(),
                            contractInfoBuilder.contractType.getTypeName(),
                            method.getParameterCount()));


        var parameter = method.getParameters()[i];

        return getOrCreateParameterInfoBuilder(parameter);
    }

    /**
     * Creates or returns a preexisting {@link ParameterInfoBuilder} for a parameter of the contract method targeted by
     * this {@code MethodInfo}
     *
     * @param parameter parameter of the method targeted by this {@code MethodInfo}
     * @return {@link ParameterInfoBuilder} for the specified parameter
     */
    protected TParameterInfoBuilder getOrCreateParameterInfoBuilder(Parameter parameter) {
        TParameterInfoBuilder parameterInfoBuilder;
        if (parameterInfoBuilders.containsKey(parameter)) {
            parameterInfoBuilder = parameterInfoBuilders.get(parameter);
        } else {
            parameterInfoBuilder = builderForParameterInternal(parameter);
            this.parameterInfoBuilders.put(parameter, parameterInfoBuilder);
        }

        return parameterInfoBuilder;
    }

    /**
     * Returns the {@code MethodInfo} object declared through the fluent API. <br> Must only be called after {@link
     * MethodInfoBuilder#build()}.
     *
     * @return {@code MethodInfo} object represented by the builder
     * @throws IllegalStateException in case the {@code build()} has not been called before.
     */
    public TMethodInfo getMethodInfo() {
        if (this.methodInfo == null) {
            throw new IllegalStateException("build() must be called before retrieving the MethodInfo!");
        }
        return this.methodInfo;
    }

    /**
     * Returns if the {@code MethodInfo} object represented by the builder has already been built or not.
     *
     * @return boolean value indicating if the {@code MethodInfo} object represented by the builder has already been
     *         built or not
     */
    public boolean hasBeenBuilt() {
        return this.methodInfo != null;
    }

    /**
     * Creates the {@link MethodInfo} object declared through the fluent API. <br> Must only be called once.
     *
     * @return {@code MethodInfo} object represented by the builder
     * @throws IllegalStateException in case the {@code build()} has been called before.
     */
    public final TContractInfoBuilder build() {
        if (this.methodInfo != null) {
            throw new IllegalStateException("build() must not be called more than once!");
        }

        this.methodInfo = buildInternal();
        return this.contractInfoBuilder;
    }

    /**
     * Creates and initializes a new {@link ParameterInfoBuilder} for the given parameter.
     *
     * @param parameter parameter
     * @return new {@link ParameterInfoBuilder} for the given parameter.
     */
    protected abstract TParameterInfoBuilder builderForParameterInternal(Parameter parameter);

    /**
     * Creates the concrete {@code MethodInfo} represented by the builder.
     *
     * @return {@code MethodInfo} represented by the builder
     */
    protected abstract TMethodInfo buildInternal();


}
