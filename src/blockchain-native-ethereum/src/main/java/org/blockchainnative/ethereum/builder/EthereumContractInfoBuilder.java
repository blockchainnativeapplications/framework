package org.blockchainnative.ethereum.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.blockchainnative.annotations.*;
import org.blockchainnative.builder.*;
import org.blockchainnative.ethereum.util.AbiUtil;
import org.blockchainnative.ethereum.Constants;
import org.blockchainnative.ethereum.metadata.*;
import org.blockchainnative.util.ReflectionUtil;
import org.blockchainnative.util.StringUtil;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Fluent API to build {@code EthereumContractInfo} objects.
 *
 * @param <TContractType> Java interface type representing the smart contract
 * @author Matthias Veit
 * @see EthereumContractInfo
 * @since 1.0
 */
public class EthereumContractInfoBuilder<TContractType> extends ContractInfoBuilder<
        EthereumContractInfoBuilder<TContractType>,
        TContractType,
        EthereumContractInfo<TContractType>,
        EthereumContractInfoBuilder<TContractType>.EthereumMethodInfoBuilder,
        EthereumMethodInfo,
        EthereumContractInfoBuilder<TContractType>.EthereumEventInfoBuilder,
        EthereumEventInfo> {
    private String address;
    private String abi;
    private String binary;
    protected AbiDefinition[] abiDefinitions;

    /**
     * Initializes a new {@code EthereumContractInfoBuilder} for the given contractType and creates builders
     * for each method annotated with {@link ContractMethod} and {@link ContractEvent}.
     *
     * @param contractType Java interface representing the smart contract
     */
    public EthereumContractInfoBuilder(Class<TContractType> contractType) {
        super(contractType);
    }

    /**
     * Sets the address at which the smart contract is deployed. <br>
     * If the address is null, the contract is considered as not deployed.
     *
     * @param address hex string representing the contract address
     * @return this {@code EthereumContractInfoBuilder}
     */
    public EthereumContractInfoBuilder<TContractType> atAddress(String address) {
        this.address = address;
        return this;
    }

    /**
     * Specifies the application binary interface (ABI) of the corresponding smart contract. <br>
     * The ABI is required in order to generate a wrapper for the smart contract.
     *
     * @param abiFile File containing the contract's ABI, the encoding is assumed to be UTF-8
     * @return this {@code EthereumContractInfoBuilder}
     * @throws IOException              in case an error occurs while reading the file
     * @throws IllegalArgumentException in case the ABI could not be parsed.
     */
    public EthereumContractInfoBuilder<TContractType> withAbi(File abiFile) throws IOException {
        return this.withAbi(abiFile, null);
    }

    /**
     * Specifies the application binary interface (ABI) of the corresponding smart contract. <br>
     * The ABI is required in order to generate a wrapper for the smart contract.
     *
     * @param abiFile  File containing the contract's ABI
     * @param encoding Encoding of the given file
     * @return this {@code EthereumContractInfoBuilder}
     * @throws IOException              in case an error occurs while reading the file
     * @throws IllegalArgumentException in case the ABI could not be parsed.
     */
    public EthereumContractInfoBuilder<TContractType> withAbi(File abiFile, Charset encoding) throws IOException {
        var abiFileContent = readFileContent(abiFile, encoding);
        return this.withAbi(abiFileContent);
    }

    /**
     * Specifies the application binary interface (ABI) of the corresponding smart contract. <br>
     * The ABI is required in order to generate a wrapper for the smart contract.
     *
     * @param abi JSON string representing the contract's ABI
     * @return this {@code EthereumContractInfoBuilder}
     * @throws IllegalArgumentException in case the ABI could not be parsed.
     */
    public EthereumContractInfoBuilder<TContractType> withAbi(String abi) {
        this.abi = abi;

        try {
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            this.abiDefinitions = objectMapper.readValue(abi, AbiDefinition[].class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse contract abi!", e);
        }

        return this;
    }

    /**
     * Specifies the binary of the corresponding smart contract. <br>
     * The binary is optional and only required if the contract is intended to be deployed using the generated wrapper.
     *
     * @param binaryFile File containing the contract's binary as hex string, the encoding is assumed to be UTF-8
     * @return this {@code EthereumContractInfoBuilder}
     * @throws IOException in case an error occurs while reading the file
     */
    public EthereumContractInfoBuilder<TContractType> withBinary(File binaryFile) throws IOException {
        return withBinary(binaryFile, null);
    }

    /**
     * Specifies the binary of the corresponding smart contract. <br>
     * The binary is optional and only required if the contract is intended to be deployed using the generated wrapper.
     *
     * @param binaryFile File containing the contract's binary as hex string
     * @param encoding   Encoding of the given file
     * @return this {@code EthereumContractInfoBuilder}
     * @throws IOException in case an error occurs while reading the file
     */
    public EthereumContractInfoBuilder<TContractType> withBinary(File binaryFile, Charset encoding) throws IOException {
        var binaryFileContent = readFileContent(binaryFile, encoding);
        return withBinary(binaryFileContent);
    }

    /**
     * Specifies the binary of the corresponding smart contract. <br>
     * The binary is optional and only required if the contract is intended to be deployed using the generated wrapper.
     *
     * @param binary hex string representing the contract's binary
     * @return this {@code EthereumContractInfoBuilder}
     */
    public EthereumContractInfoBuilder<TContractType> withBinary(String binary) {
        this.binary = binary;
        return this;
    }

    /**
     * Specifies a method as deployment method, i.e. a method that is meant to invoke the smart contract's constructor. <br>
     * <br>
     * This method is a shortcut for calling
     * {@code EthereumMethodInfoBuilder.method(method).name(Constants.DEPLOYMENT_METHOD).specialMethod(true)}
     *
     * @param method method of the smart contract interface
     * @return {@link EthereumMethodInfoBuilder} for the given method.
     */
    public EthereumMethodInfoBuilder deploymentMethod(Method method) {
        return super.method(method)
                .name(Constants.DEPLOYMENT_METHOD)
                .specialMethod(true);
    }

    /**
     * Specifies a method as deployment method, i.e. a method that is meant to invoke the smart contract's constructor. <br>
     * <br>
     * This method is a shortcut for calling
     * {@code EthereumMethodInfoBuilder.method(methodName, parameterTypes).name(Constants.DEPLOYMENT_METHOD).specialMethod(true)}
     *
     * @param methodName     name of the method
     * @param parameterTypes types of the method parameters
     * @return {@link EthereumMethodInfoBuilder} for the given method.
     */
    public EthereumMethodInfoBuilder deploymentMethod(String methodName, Class<?>... parameterTypes) {
        return super.method(methodName, parameterTypes)
                .name(Constants.DEPLOYMENT_METHOD)
                .specialMethod(true);
    }

    /**
     * Creates and initializes a new {@link EthereumMethodInfoBuilder} for the given method.
     *
     * @param method method of the smart contract interface
     * @return new {@link EthereumMethodInfoBuilder} for the given method.
     */
    @Override
    protected EthereumMethodInfoBuilder builderForMethodInternal(Method method) {
        return new EthereumMethodInfoBuilder(this, method);
    }

    /**
     * Creates and initializes a new {@link EthereumEventInfoBuilder} for the given event method.
     *
     * @param method event method of the smart contract interface
     * @return new {@link EthereumEventInfoBuilder} for the given event method.
     */
    @Override
    protected EthereumEventInfoBuilder builderForEventInternal(Method method) {
        return new EthereumEventInfoBuilder(this, method);
    }

    /**
     * Builds the actual {@code EthereumContractInfo}.
     *
     * @return {@code EthereumContractInfo} represented by this builder
     */
    @Override
    protected EthereumContractInfo<TContractType> buildInternal() {
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

        if (StringUtil.isNullOrEmpty(abi))
            throw new IllegalStateException("Contract ABI must not be null or empty!");

        if (StringUtil.isNullOrEmpty(identifier))
            this.identifier = getRandomId();

        return new EthereumContractInfo<>(identifier, contractType, methodInfos, eventInfos, address, abi, binary);
    }


    private String readFileContent(File file, Charset encoding) throws IOException {
        if (encoding == null)
            encoding = StandardCharsets.UTF_8;
        return new String(Files.readAllBytes(file.toPath()), encoding);
    }

    /**
     * Fluent API to build {@code EthereumMethodInfo} objects.
     *
     * @author Matthias Veit
     * @see EthereumContractInfo
     * @see EthereumMethodInfo
     * @since 1.0
     */
    public class EthereumMethodInfoBuilder extends MethodInfoBuilder<EthereumMethodInfoBuilder, EthereumMethodInfo, EthereumContractInfoBuilder<TContractType>, EthereumParameterInfoBuilder> {
        private AbiDefinition abi;

        /**
         * Initializes a new {@code EthereumMethodInfoBuilder} for the given method and assigns the values
         * from the metadata annotation {@link ContractMethod}
         *
         * @param contractInfoBuilder parent builder
         * @param method              smart contract method
         */
        EthereumMethodInfoBuilder(EthereumContractInfoBuilder<TContractType> contractInfoBuilder, Method method) {
            super(contractInfoBuilder, method);
        }

        /**
         * Creates and initializes a new {@link EthereumParameterInfoBuilder} for the given parameter.
         *
         * @param parameter parameter
         * @return new {@link EthereumParameterInfoBuilder} for the given parameter.
         */
        @Override
        protected EthereumParameterInfoBuilder builderForParameterInternal(Parameter parameter) {
            return new EthereumParameterInfoBuilder(this, parameter);
        }

        /**
         * Tries to find the a matching entry in the contract's ABI for the given parameter and returns its type.
         *
         * @param p parameter to get the type for.
         * @return type of the parameter as defined in the contract's ABI
         */
        protected String getParameterEthereumType(Parameter p) {
            if (this.abi == null) {
                // get AbiDefinition for this eventMethod from parent
                tryGetAbiFromContractBuilder();
            }

            if (this.abi == null)
                throw new IllegalStateException(String.format("Could not find ABI definition for method '%s'", contractMethodName));

            var parameters = this.method.getParameters();
            var parameterIndex = -1;
            var additionalArgsCount = 0;
            for (var i = 0; i < parameters.length; i++) {
                var parameterInfo = this.parameterInfoBuilders.get(parameters[i]);
                if (parameterInfo.isSpecialArgument()) {
                    additionalArgsCount++;
                }

                if (parameters[i].equals(p)) {
                    parameterIndex = i;
                    break;
                }
            }
            // Subtract the additional arguments not declared by the contract method
            // in order to get the real field index
            parameterIndex = parameterIndex - additionalArgsCount;

            if (parameterIndex >= this.abi.getInputs().size()) {
                throw new IllegalStateException("ABI definition defines too few input parameters.");
            }

            var type = this.abi.getInputs().get(parameterIndex).getType();

            return AbiUtil.stripLocationFromType(type);
        }

        private void tryGetAbiFromContractBuilder() {
            if(contractInfoBuilder.abiDefinitions == null){
                throw new IllegalStateException("Contract ABI has not been set! The ABI is required by the ContractInfoBuilder, specify it before calling build().");
            }

            if (this.isSpecialMethod) {
                if (Constants.DEPLOYMENT_METHOD.equalsIgnoreCase(this.contractMethodName)) {
                    this.abi = Arrays.stream(contractInfoBuilder.abiDefinitions)
                            .filter(abiDefinition -> "constructor".equals(abiDefinition.getType()))
                            .findAny().orElse(null);
                }
            } else {
                this.abi = Arrays.stream(contractInfoBuilder.abiDefinitions)
                        .filter(abiDefinition -> "function".equals(abiDefinition.getType()))
                        .filter(abiDefinition -> contractMethodName.equalsIgnoreCase(abiDefinition.getName()))
                        .findAny().orElse(null);
            }
        }

        /**
         * Creates the {@code EthereumMethodInfo} represented by the builder.
         *
         * @return {@code EthereumMethodInfo} represented by the builder
         */
        @Override
        protected EthereumMethodInfo buildInternal() {
            if (StringUtil.isNullOrEmpty(this.contractMethodName)) {
                throw new IllegalStateException(String.format("Contract method name not has not been set for method '%s'", method.getName()));
            }

            // get AbiDefinition for this eventMethod from parent
            tryGetAbiFromContractBuilder();

            if (!isSpecialMethod && this.abi == null) {
                throw new IllegalStateException(String.format("Could not find ABI definition for method '%s'", contractMethodName));
            }

            if (this.isSpecialMethod && Constants.DEPLOYMENT_METHOD.equalsIgnoreCase(this.contractMethodName)) {
                // check return type
                var methodReturnType = ReflectionUtil.getActualReturnType(this.method);
                if (!(Void.TYPE == methodReturnType || String.class == methodReturnType)) {
                    throw new IllegalStateException(String.format("Unexpected return type of deployment method '%s'(...). The deployment method must either return String or void wrapped in Future<>, Result<> or both.", method.getName()));
                }
            }

            var parameterInfos = this.parameterInfoBuilders.values().stream().map(builder -> {
                if (!builder.hasBeenBuilt())
                    builder.build();
                return builder.getParameterInfo();
            }).collect(Collectors.toList());

            return new EthereumMethodInfo(method, contractMethodName, isReadOnly, isSpecialMethod, parameterInfos, abi, resultTypeConverterClass);
        }
    }

    /**
     * Fluent API to build {@code EthereumParameterInfo} objects.
     *
     * @author Matthias Veit
     * @see EthereumContractInfo
     * @see EthereumParameterInfo
     * @since 1.0
     */
    public class EthereumParameterInfoBuilder extends ParameterInfoBuilder<EthereumParameterInfoBuilder, EthereumParameterInfo, EthereumMethodInfoBuilder> {

        /**
         * Initializes a new {@code EthereumParameterInfoBuilder} for the given parameter and assigns the values
         * from the metadata annotation {@link ContractParameter} and {@link SpecialArgument}
         *
         * @param ethereumMethodInfoBuilder parent builder
         * @param parameter                 smart contract method parameter
         */
        EthereumParameterInfoBuilder(EthereumMethodInfoBuilder ethereumMethodInfoBuilder, Parameter parameter) {
            super(ethereumMethodInfoBuilder, parameter);
        }

        /**
         * Creates the {@code EthereumParameterInfo} represented by the builder.
         *
         * @return {@code EthereumParameterInfo} represented by the builder
         */
        @Override
        protected EthereumParameterInfo buildInternal() {
            String ethereumParameterType;
            if (isSpecialArgument()) {
                ethereumParameterType = null;
            } else {
                ethereumParameterType = this.methodInfoBuilder.getParameterEthereumType(this.parameter);
            }
            return new EthereumParameterInfo(parameter, parameterIndex, ethereumParameterType, typeConverterClass, passAsType, specialArgName);
        }
    }

    /**
     * Fluent API to build {@code EthereumEventInfo} objects.
     *
     * @author Matthias Veit
     * @see EthereumContractInfo
     * @see EthereumEventInfo
     * @since 1.0
     */
    public class EthereumEventInfoBuilder extends EventInfoBuilder<EthereumEventInfoBuilder, EthereumEventInfo, EthereumContractInfoBuilder<TContractType>, EthereumEventFieldInfoBuilder, EthereumEventParameterInfoBuilder> {
        protected AbiDefinition abi;

        /**
         * Initializes a new {@code EthereumEventInfoBuilder} for the given event method and assigns the values
         * from the metadata annotation {@link org.blockchainnative.annotations.ContractEvent}
         *
         * @param contractInfoBuilder parent builder
         * @param eventMethod         smart contract event method
         */
        EthereumEventInfoBuilder(EthereumContractInfoBuilder<TContractType> contractInfoBuilder, Method eventMethod) {
            super(contractInfoBuilder, eventMethod);
        }

        /**
         * Creates and initializes a new {@link EthereumEventParameterInfoBuilder} for the given parameter.
         *
         * @param parameter parameter
         * @return new {@link EthereumEventParameterInfoBuilder} for the given parameter.
         */
        @Override
        protected EthereumEventParameterInfoBuilder builderForParameterInternal(Parameter parameter) {
            return new EthereumEventParameterInfoBuilder(this, parameter);
        }

        /**
         * Creates and initializes a new {@link EthereumEventFieldInfoBuilder} for the given field.
         *
         * @param field field
         * @return new {@link EthereumEventFieldInfoBuilder} for the given field.
         */
        @Override
        protected EthereumEventFieldInfoBuilder builderForFieldInternal(Field field) {
            return new EthereumEventFieldInfoBuilder(this, field);
        }

        private void tryGetAbiFromContractBuilder() {
            if(contractInfoBuilder.abiDefinitions == null){
                throw new IllegalStateException("Contract ABI has not been set! The ABI is required by the ContractInfoBuilder, specify it before calling build().");
            }

            this.abi = Arrays.stream(contractInfoBuilder.abiDefinitions)
                    .filter(abiDefinition -> "event".equals(abiDefinition.getType()))
                    .filter(abiDefinition -> this.eventName.equalsIgnoreCase(abiDefinition.getName()))
                    .findAny().orElse(null);
        }

        /**
         * Creates the {@code EthereumEventInfo} represented by the builder.
         *
         * @return {@code EthereumEventInfo} represented by the builder
         */
        @Override
        protected EthereumEventInfo buildInternal() {
            if (StringUtil.isNullOrEmpty(this.eventName)) {
                throw new IllegalStateException(String.format("Event name not has not been set for event method '%s'", this.eventMethod.getName()));
            }

            // get AbiDefinition for this eventMethod from parent
            tryGetAbiFromContractBuilder();

            if (this.abi == null) {
                throw new IllegalStateException(String.format("Could not find ABI definition for event '%s'", this.eventName));
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


            return new EthereumEventInfo(eventName, eventMethod, eventParameterInfos, eventFieldInfos, abi);
        }
    }

    /**
     * Fluent API to build {@code EthereumEventParameterInfo} objects.
     *
     * @author Matthias Veit
     * @see EthereumContractInfo
     * @see EthereumEventParameterInfo
     * @since 1.0
     */
    public class EthereumEventParameterInfoBuilder extends EventParameterInfoBuilder<EthereumEventParameterInfoBuilder, EthereumEventParameterInfo, EthereumEventInfoBuilder> {

        /**
         * Initializes a new {@code EthereumEventParameterInfoBuilder} for the given parameter and assigns the values
         * from the metadata annotation {@link EventParameter} and {@link SpecialArgument}
         *
         * @param eventInfoBuilder parent builder
         * @param eventParameter   smart contract event parameter
         */
        EthereumEventParameterInfoBuilder(EthereumEventInfoBuilder eventInfoBuilder, Parameter eventParameter) {
            super(eventInfoBuilder, eventParameter);
        }

        /**
         * Creates the {@code EthereumEventParameterInfo} represented by the builder.
         *
         * @return {@code EthereumEventParameterInfo} represented by the builder
         */
        @Override
        protected EthereumEventParameterInfo buildInternal() {

            if (StringUtil.isNullOrEmpty(this.argumentName)) {
                throw new IllegalStateException(String.format("Argument name for parameter %s (%s) of event method '%s(...)' is not set.", this.parameterIndex, this.parameter.getType().getName(), this.eventInfoBuilder.getEventMethod().getName()));
            }

            return new EthereumEventParameterInfo(parameter, parameterIndex, argumentName);
        }
    }

    /**
     * Fluent API to build {@code EthereumEventFieldInfo} objects.
     *
     * @author Matthias Veit
     * @see EthereumContractInfo
     * @see EthereumEventFieldInfo
     * @since 1.0
     */
    public class EthereumEventFieldInfoBuilder extends EventFieldInfoBuilder<EthereumEventFieldInfoBuilder, EthereumEventFieldInfo, EthereumEventInfoBuilder> {
        protected AbiDefinition.NamedType solidityType;

        /**
         * Initializes a new {@code EthereumEventFieldInfoBuilder} for the given field and assigns the values
         * from the metadata annotation {@link EventField}
         *
         * @param eventInfoBuilder parent builder
         * @param field            event field
         */
        EthereumEventFieldInfoBuilder(EthereumEventInfoBuilder eventInfoBuilder, Field field) {
            super(eventInfoBuilder, field);
        }

        private void tryGetSolidityTypeFromEventBuilder() {
            if(eventInfoBuilder.abi == null){
                throw new IllegalStateException(String.format("ABI definition for event method '%s' could not found! Make sure to specify the correct ABI on the ContractInfoBuilder, before calling build().", this.eventInfoBuilder.getEventMethod().getName()));
            }
            if (StringUtil.isNullOrEmpty(this.sourceFieldName)) {
                if (this.sourceFieldIndex.isPresent() && this.sourceFieldIndex.get() > -1 && this.sourceFieldIndex.get() < eventInfoBuilder.abi.getInputs().size()) {
                    this.solidityType = eventInfoBuilder.abi.getInputs().get(this.sourceFieldIndex.get());
                }
            } else {
                this.solidityType = eventInfoBuilder.abi.getInputs().stream()
                        .filter(abiDefinition -> this.sourceFieldName.equalsIgnoreCase(abiDefinition.getName()))
                        .findAny().orElse(null);
            }
        }

        /**
         * Creates the {@code EthereumEventFieldInfo} represented by the builder.
         *
         * @return {@code EthereumEventFieldInfo} represented by the builder
         */
        @Override
        protected EthereumEventFieldInfo buildInternal() {
            // get Solidity NamedType for this field from parent
            tryGetSolidityTypeFromEventBuilder();

            if (this.solidityType == null) {
                throw new IllegalStateException(String.format("Could not find ABI type definition for event field '%s'. Specified name name: '%s', index: '%s'", field.getName(), Objects.toString(sourceFieldName, "null"), sourceFieldIndex.isPresent() ? sourceFieldIndex.get() : "<not set>"));
            }

            if (StringUtil.isNullOrEmpty(sourceFieldName)) {
                sourceFieldName = null;
            }

            return new EthereumEventFieldInfo(field, sourceFieldName, sourceFieldIndex, Optional.ofNullable(typeConverterClass), solidityType);
        }
    }
}
