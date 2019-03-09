package org.blockchainnative.fabric.builder;


import org.blockchainnative.annotations.*;
import org.blockchainnative.builder.*;
import org.blockchainnative.fabric.Constants;
import org.blockchainnative.fabric.metadata.*;
import org.blockchainnative.util.ReflectionUtil;
import org.blockchainnative.util.StringUtil;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fluent API to build {@code FabricContractInfo} objects.
 *
 * @param <TContractType> Java interface type representing the chaincode
 * @author Matthias Veit
 * @see FabricContractInfo
 * @since 1.0
 */
public class FabricContractInfoBuilder<TContractType> extends ContractInfoBuilder<FabricContractInfoBuilder<TContractType>, TContractType, FabricContractInfo<TContractType>, FabricContractInfoBuilder<TContractType>.FabricMethodInfoBuilder, FabricMethodInfo, FabricContractInfoBuilder<TContractType>.FabricEventInfoBuilder, FabricEventInfo> {
    private ChaincodeID chaincodeID;
    private ChaincodeLanguage chaincodeLanguage;
    private Set<String> targetPeerNames;
    private String chaincodeSourceDirectory;
    private ChaincodeEndorsementPolicy policy;

    /**
     * Initializes a new {@code FabricContractInfoBuilder} for the given contractType and creates builders for each
     * method annotated with {@link ContractMethod} and {@link ContractEvent}.
     *
     * @param contractType Java interface representing the chaincode
     */
    public FabricContractInfoBuilder(Class<TContractType> contractType) {
        super(contractType);

        parseAnnotations();
    }

    private void parseAnnotations() {
        var chaincodeLanguageAnnotation = this.contractType.getAnnotation(org.blockchainnative.fabric.annotations.ChaincodeLanguage.class);
        if (chaincodeLanguageAnnotation != null) {
            this.chaincodeLanguage = chaincodeLanguageAnnotation.value();
        }
    }

    /**
     * Creates and initializes a new {@link FabricMethodInfoBuilder} for the given method.
     *
     * @param method method of the smart contract interface
     * @return new {@link FabricMethodInfoBuilder} for the given method.
     */
    @Override
    protected FabricMethodInfoBuilder builderForMethodInternal(Method method) {
        return new FabricMethodInfoBuilder(this, method);
    }

    /**
     * Creates and initializes a new {@link FabricEventInfoBuilder} for the given event method.
     *
     * @param method event method of the smart contract interface
     * @return new {@link FabricEventInfoBuilder} for the given event method.
     */
    @Override
    protected FabricEventInfoBuilder builderForEventInternal(Method method) {
        return new FabricEventInfoBuilder(this, method);
    }


    /**
     * Specifies the {@code ChaincodeID} of the corresponding chaincode. <br> The chaincodeID is required in order to
     * generate a wrapper for the chaincode.
     *
     * @param chainCodeIdentifier {@code ChaincodeID} containing the name, version and path
     * @return this {@code FabricContractInfoBuilder}
     */
    public FabricContractInfoBuilder<TContractType> withChainCodeIdentifier(ChaincodeID chainCodeIdentifier) {
        this.chaincodeID = chainCodeIdentifier;
        return this;
    }

    /**
     * Specifies the {@code ChaincodeLanguage} of the corresponding chaincode. <br> The chaincode language is optional
     * and only required if the chaincode is intended to be installed/instantiated using the generated wrapper. <br>
     * Initial value is taken from {@link org.blockchainnative.fabric.annotations.ChaincodeLanguage#value()}
     *
     * @param chaincodeLanguage language in which the chaincode is defined
     * @return this {@code FabricContractInfoBuilder}
     */
    public FabricContractInfoBuilder<TContractType> withChainCodeLanguage(ChaincodeLanguage chaincodeLanguage) {
        this.chaincodeLanguage = chaincodeLanguage;
        return this;
    }

    /**
     * Specifies the names of the peers to be targeted by chaincode methods. <br> The chaincode language is optional and
     * only required if the chaincode is intended to be installed/instantiated using the generated wrapper.
     *
     * @param targetPeerNames names of the peers to be targeted by chaincode methods
     * @return this {@code FabricContractInfoBuilder}
     */
    public FabricContractInfoBuilder<TContractType> withTargetPeers(Set<String> targetPeerNames) {
        this.targetPeerNames = targetPeerNames;
        return this;
    }


    /**
     * Specifies the directory containing the chaincode source files. <br> The source directory is optional and only
     * required if the chaincode is intended to be installed/instantiated using the generated wrapper.
     *
     * @param sourceDirectory directory containing the chaincode source files.
     * @return this {@code FabricContractInfoBuilder}
     */
    public FabricContractInfoBuilder<TContractType> withChaincodeSourceDirectory(String sourceDirectory) {
        this.chaincodeSourceDirectory = sourceDirectory;
        return this;
    }

    /**
     * Specifies the {@code ChaincodeEndorsementPolicy} used to verify chaincode invocations. <br> The policy is
     * optional and only required if the chaincode is intended to be installed/instantiated using the generated
     * wrapper.
     *
     * @param policy {@code ChaincodeEndorsementPolicy} used when instantiating the chaincode.
     * @return this {@code FabricContractInfoBuilder}
     */
    public FabricContractInfoBuilder<TContractType> withChaincodePolicy(ChaincodeEndorsementPolicy policy) {
        this.policy = policy;
        return this;
    }

    /**
     * Specifies a method as install method, i.e. a method that is meant to install the chaincode on the target peers. <br>
     * <br>
     * This method is a shortcut for calling
     * {@code FabricMethodInfoBuilder.method(method).name(Constants.INSTALL_METHOD).specialMethod(true)}
     *
     * @param method method of the smart contract interface
     * @return {@link FabricMethodInfoBuilder} for the given method.
     */
    public FabricMethodInfoBuilder installMethod(Method method) {
        return super.method(method)
                .name(Constants.INSTALL_METHOD)
                .specialMethod(true);
    }

    /**
     * Specifies a method as install method, i.e. a method that is meant to install the chaincode on the target peers. <br>
     * <br>
     * This method is a shortcut for calling
     * {@code FabricMethodInfoBuilder.method(method).name(Constants.INSTALL_METHOD).specialMethod(true)}
     *
     * @param methodName     name of the method
     * @param parameterTypes types of the method parameters
     * @return {@link FabricMethodInfoBuilder} for the given method.
     */
    public FabricMethodInfoBuilder installMethod(String methodName, Class<?>... parameterTypes) {
        return super.method(methodName, parameterTypes)
                .name(Constants.INSTALL_METHOD)
                .specialMethod(true);
    }

    /**
     * Specifies a method as instantiate method, i.e. a method that is meant to instantiate the chaincode on the target peers. <br>
     * <br>
     * This method is a shortcut for calling
     * {@code FabricMethodInfoBuilder.method(method).name(Constants.INSTANTIATE_METHOD).specialMethod(true)}
     *
     * @param method method of the smart contract interface
     * @return {@link FabricMethodInfoBuilder} for the given method.
     */
    public FabricMethodInfoBuilder instantiateMethod(Method method) {
        return super.method(method)
                .name(Constants.INSTANTIATE_METHOD)
                .specialMethod(true);
    }

    /**
     * Specifies a method as instantiate method, i.e. a method that is meant to instantiate the chaincode on the target peers. <br>
     * <br>
     * This method is a shortcut for calling
     * {@code FabricMethodInfoBuilder.method(method).name(Constants.INSTANTIATE_METHOD).specialMethod(true)}
     *
     * @param methodName     name of the method
     * @param parameterTypes types of the method parameters
     * @return {@link FabricMethodInfoBuilder} for the given method.
     */
    public FabricMethodInfoBuilder instantiateMethod(String methodName, Class<?>... parameterTypes) {
        return super.method(methodName, parameterTypes)
                .name(Constants.INSTANTIATE_METHOD)
                .specialMethod(true);
    }

    /**
     * Builds the actual {@code FabricContractInfo}.
     *
     * @return {@code FabricContractInfo} represented by this builder
     */
    @Override
    protected FabricContractInfo<TContractType> buildInternal() {
        var methodInfos = this.methodInfoBuilders.values().stream().map(builder -> {
            if (!builder.hasBeenBuilt())
                builder.build();
            return builder.getMethodInfo();
        }).collect(Collectors.toSet());

        var eventInfos = this.eventInfoBuilders.values().stream().map(builder -> {
            if (!builder.hasBeenBuilt())
                builder.build();
            return builder.getEventInfo();
        }).collect(Collectors.toSet());

        if (StringUtil.isNullOrEmpty(identifier)) {
            this.identifier = getRandomId();
        }

        if (chaincodeID == null) {
            throw new IllegalStateException("ChaincodeID needs to be specified");
        }

        if (targetPeerNames == null) {
            targetPeerNames = new HashSet<>();
        }

        return new FabricContractInfo<>(identifier, contractType, methodInfos, eventInfos, chaincodeID, chaincodeLanguage, policy, chaincodeSourceDirectory, targetPeerNames);
    }

    /**
     * Fluent API to build {@code FabricMethodInfo} objects.
     *
     * @author Matthias Veit
     * @see FabricContractInfo
     * @see FabricMethodInfo
     * @since 1.0
     */
    public class FabricMethodInfoBuilder extends MethodInfoBuilder<FabricMethodInfoBuilder, FabricMethodInfo, FabricContractInfoBuilder<TContractType>, FabricParameterInfoBuilder> {

        /**
         * Initializes a new {@code FabricMethodInfoBuilder} for the given method and assigns the values
         * from the metadata annotation {@link ContractMethod}
         *
         * @param contractInfoBuilder parent builder
         * @param method              smart contract method
         */
        FabricMethodInfoBuilder(FabricContractInfoBuilder<TContractType> contractInfoBuilder, Method method) {
            super(contractInfoBuilder, method);
        }

        /**
         * Creates and initializes a new {@link FabricParameterInfoBuilder} for the given parameter.
         *
         * @param parameter parameter
         * @return new {@link FabricParameterInfoBuilder} for the given parameter.
         */
        @Override
        protected FabricParameterInfoBuilder builderForParameterInternal(Parameter parameter) {
            return new FabricParameterInfoBuilder(this, parameter);
        }

        /**
         * Creates the {@code FabricMethodInfo} represented by the builder.
         *
         * @return {@code FabricMethodInfo} represented by the builder
         */
        @Override
        protected FabricMethodInfo buildInternal() {
            if (this.contractMethodName == null) {
                throw new IllegalStateException(String.format("Contract method name not has not been set for eventMethod '%s'", method.getName()));
            }

            if (this.isSpecialMethod &&
                    (Constants.INSTANTIATE_METHOD.equalsIgnoreCase(this.contractMethodName) || Constants.INSTALL_METHOD.equalsIgnoreCase(this.contractMethodName))) {
                // check return type
                var methodReturnType = ReflectionUtil.getActualReturnType(this.method);
                if (!(Void.TYPE == methodReturnType)) {
                    throw new IllegalStateException(String.format("Unexpected return type of deployment method '%s'(...). The deployment method must return void wrapped in Future<>, Result<> or both.", method.getName()));
                }
            }

            var parameterInfos = this.parameterInfoBuilders.values().stream().map(builder -> {
                if (!builder.hasBeenBuilt())
                    builder.build();
                return builder.getParameterInfo();
            }).collect(Collectors.toList());
            return new FabricMethodInfo(method, contractMethodName, isReadOnly, isSpecialMethod, parameterInfos, resultTypeConverterClass);
        }
    }

    /**
     * Fluent API to build {@code FabricParameterInfo} objects.
     *
     * @author Matthias Veit
     * @see FabricContractInfo
     * @see FabricParameterInfo
     * @since 1.0
     */
    public class FabricParameterInfoBuilder extends ParameterInfoBuilder<FabricParameterInfoBuilder, FabricParameterInfo, FabricMethodInfoBuilder> {

        /**
         * Initializes a new {@code FabricParameterInfoBuilder} for the given parameter and assigns the values
         * from the metadata annotation {@link ContractParameter} and {@link SpecialArgument}
         *
         * @param methodInfoBuilder parent builder
         * @param parameter                 smart contract method parameter
         */
        FabricParameterInfoBuilder(FabricMethodInfoBuilder methodInfoBuilder, Parameter parameter) {
            super(methodInfoBuilder, parameter);
        }

        /**
         * Creates the {@code FabricParameterInfo} represented by the builder.
         *
         * @return {@code FabricParameterInfo} represented by the builder
         */
        @Override
        protected FabricParameterInfo buildInternal() {
            return new FabricParameterInfo(parameter, parameterIndex, typeConverterClass, passAsType, specialArgName);
        }
    }

    /**
     * Fluent API to build {@code FabricEventInfoBuilder} objects.
     *
     * @author Matthias Veit
     * @see FabricContractInfo
     * @see FabricEventInfo
     * @since 1.0
     */
    public class FabricEventInfoBuilder extends EventInfoBuilder<FabricEventInfoBuilder, FabricEventInfo, FabricContractInfoBuilder<TContractType>, FabricEventFieldInfoBuilder, FabricEventParameterInfoBuilder> {

        /**
         * Initializes a new {@code FabricEventInfoBuilder} for the given event method and assigns the values
         * from the metadata annotation {@link org.blockchainnative.annotations.ContractEvent}
         *
         * @param contractInfoBuilder parent builder
         * @param eventMethod         smart contract event method
         */
        FabricEventInfoBuilder(FabricContractInfoBuilder<TContractType> contractInfoBuilder, Method eventMethod) {
            super(contractInfoBuilder, eventMethod);
        }

        /**
         * Creates and initializes a new {@link FabricEventParameterInfoBuilder} for the given parameter.
         *
         * @param parameter parameter
         * @return new {@link FabricEventParameterInfoBuilder} for the given parameter.
         */
        @Override
        protected FabricEventParameterInfoBuilder builderForParameterInternal(Parameter parameter) {
            return new FabricEventParameterInfoBuilder(this, parameter);
        }

        /**
         * Creates and initializes a new {@link FabricEventFieldInfoBuilder} for the given field.
         *
         * @param field field
         * @return new {@link FabricEventFieldInfoBuilder} for the given field.
         */
        @Override
        protected FabricEventFieldInfoBuilder builderForFieldInternal(Field field) {
            return new FabricEventFieldInfoBuilder(this, field);
        }

        /**
         * Creates the {@code FabricEventInfo} represented by the builder.
         *
         * @return {@code FabricEventInfo} represented by the builder
         */
        @Override
        protected FabricEventInfo buildInternal() {

            if (StringUtil.isNullOrEmpty(this.eventName)) {
                throw new IllegalStateException(String.format("Event name not has not been set for event method '%s'", this.eventMethod.getName()));
            }

            var eventParameterInfos = this.eventParameterInfoBuilders.values().stream().map(builder -> {
                if (!builder.hasBeenBuilt())
                    builder.build();
                return builder.getEventParameterInfo();
            }).collect(Collectors.toList());

            var eventFieldInfos = this.eventFieldInfoBuilders.values().stream().map(builder -> {
                if (!builder.hasBeenBuilt())
                    builder.build();
                return builder.getEventFieldInfo();
            }).collect(Collectors.toList());

            if (eventFieldInfos.size() != 1) {
                throw new IllegalStateException(String.format("Hyperledger Fabric events support only a single a event field, however, %s event fields are registered", eventFieldInfos.size()));
            }

            return new FabricEventInfo(eventName, eventMethod, eventParameterInfos, eventFieldInfos);
        }
    }

    /**
     * Fluent API to build {@code FabricEventParameterInfo} objects.
     *
     * @author Matthias Veit
     * @see FabricContractInfo
     * @see FabricEventParameterInfo
     * @since 1.0
     */
    public class FabricEventParameterInfoBuilder extends EventParameterInfoBuilder<FabricEventParameterInfoBuilder, FabricEventParameterInfo, FabricEventInfoBuilder> {

        /**
         * Initializes a new {@code FabricEventParameterInfoBuilder} for the given parameter and assigns the values
         * from the metadata annotation {@link EventParameter} and {@link SpecialArgument}
         *
         * @param eventInfoBuilder parent builder
         * @param eventParameter   smart contract event parameter
         */
        FabricEventParameterInfoBuilder(FabricEventInfoBuilder eventInfoBuilder, Parameter eventParameter) {
            super(eventInfoBuilder, eventParameter);
        }

        /**
         * Creates the {@code FabricEventParameterInfo} represented by the builder.
         *
         * @return {@code FabricEventParameterInfo} represented by the builder
         */
        @Override
        protected FabricEventParameterInfo buildInternal() {

            if (StringUtil.isNullOrEmpty(this.argumentName)) {
                throw new IllegalStateException(String.format("Argument name for parameter %s (%s) of event method '%s(...)' is not set.", this.parameterIndex, this.parameter.getType().getName(), this.eventInfoBuilder.getEventMethod().getName()));
            }

            return new FabricEventParameterInfo(parameter, parameterIndex, argumentName);
        }
    }

    /**
     * Fluent API to build {@code FabricEventFieldInfo} objects.
     *
     * @author Matthias Veit
     * @see FabricContractInfo
     * @see FabricEventFieldInfo
     * @since 1.0
     */
    public class FabricEventFieldInfoBuilder extends EventFieldInfoBuilder<FabricEventFieldInfoBuilder, FabricEventFieldInfo, FabricEventInfoBuilder> {

        /**
         * Initializes a new {@code FabricEventFieldInfoBuilder} for the given field and assigns the values
         * from the metadata annotation {@link EventField}
         *
         * @param eventInfoBuilder parent builder
         * @param field            event field
         */
        public FabricEventFieldInfoBuilder(FabricEventInfoBuilder eventInfoBuilder, Field field) {
            super(eventInfoBuilder, field);
        }

        /**
         * Creates the {@code FabricEventFieldInfo} represented by the builder.
         *
         * @return {@code FabricEventFieldInfo} represented by the builder
         */
        @Override
        protected FabricEventFieldInfo buildInternal() {
            // sourceFieldName and index are irrelevant as there must only be a single field

            return new FabricEventFieldInfo(field, Optional.ofNullable(typeConverterClass));
        }
    }
}
