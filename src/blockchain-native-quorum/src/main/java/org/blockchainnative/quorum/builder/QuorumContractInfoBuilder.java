package org.blockchainnative.quorum.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.blockchainnative.annotations.*;
import org.blockchainnative.builder.*;
import org.blockchainnative.ethereum.util.AbiUtil;
import org.blockchainnative.quorum.Constants;
import org.blockchainnative.quorum.metadata.*;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Fluent API to build {@code QuorumContractInfo} objects.
 *
 * @param <TContractType> Java interface type representing the smart contract
 * @author Matthias Veit
 * @see QuorumContractInfo
 * @since 1.1
 */
public class QuorumContractInfoBuilder<TContractType> extends ContractInfoBuilder<
        QuorumContractInfoBuilder<TContractType>,
        TContractType,
        QuorumContractInfo<TContractType>,
        QuorumContractInfoBuilder<TContractType>.QuorumMethodInfoBuilder,
        QuorumMethodInfo,
        QuorumContractInfoBuilder<TContractType>.QuorumEventInfoBuilder,
        QuorumEventInfo> {
    private String address;
    private String abi;
    private String binary;
    private List<String> privateFor;
    protected AbiDefinition[] abiDefinitions;


    /**
     * Initializes a new {@code QuorumContractInfoBuilder} for the given contractType and creates builders
     * for each method annotated with {@link ContractMethod} and {@link ContractEvent}.
     *
     * @param contractType Java interface representing the smart contract
     */
    public QuorumContractInfoBuilder(Class<TContractType> contractType) {
        super(contractType);
    }


    /**
     * Sets the address at which the smart contract is deployed. <br>
     * If the address is null, the contract is considered as not deployed.
     *
     * @param address hex string representing the contract address
     * @return this {@code QuorumContractInfoBuilder}
     */
    public QuorumContractInfoBuilder<TContractType> atAddress(String address) {
        this.address = address;
        return this;
    }


    /**
     * Specifies the application binary interface (ABI) of the corresponding smart contract. <br>
     * The ABI is required in order to generate a wrapper for the smart contract.
     *
     * @param abiFile File containing the contract's ABI, the encoding is assumed to be UTF-8
     * @return this {@code QuorumContractInfoBuilder}
     * @throws IOException              in case an error occurs while reading the file
     * @throws IllegalArgumentException in case the ABI could not be parsed.
     */
    public QuorumContractInfoBuilder<TContractType> withAbi(File abiFile) throws IOException {
        return this.withAbi(abiFile, null);
    }

    /**
     * Specifies the application binary interface (ABI) of the corresponding smart contract. <br>
     * The ABI is required in order to generate a wrapper for the smart contract.
     *
     * @param abiFile  File containing the contract's ABI
     * @param encoding Encoding of the given file
     * @return this {@code QuorumContractInfoBuilder}
     * @throws IOException              in case an error occurs while reading the file
     * @throws IllegalArgumentException in case the ABI could not be parsed.
     */
    public QuorumContractInfoBuilder<TContractType> withAbi(File abiFile, Charset encoding) throws IOException {
        var abiFileContent = readFileContent(abiFile, encoding);
        return this.withAbi(abiFileContent);
    }

    /**
     * Specifies the application binary interface (ABI) of the corresponding smart contract. <br>
     * The ABI is required in order to generate a wrapper for the smart contract.
     *
     * @param abi JSON string representing the contract's ABI
     * @return this {@code QuorumContractInfoBuilder}
     * @throws IllegalArgumentException in case the ABI could not be parsed.
     */
    public QuorumContractInfoBuilder<TContractType> withAbi(String abi) {
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
     * @return this {@code QuorumContractInfoBuilder}
     * @throws IOException in case an error occurs while reading the file
     */
    public QuorumContractInfoBuilder<TContractType> withBinary(File binaryFile) throws IOException {
        return withBinary(binaryFile, null);
    }

    /**
     * Specifies the binary of the corresponding smart contract. <br>
     * The binary is optional and only required if the contract is intended to be deployed using the generated wrapper.
     *
     * @param binaryFile File containing the contract's binary as hex string
     * @param encoding   Encoding of the given file
     * @return this {@code QuorumContractInfoBuilder}
     * @throws IOException in case an error occurs while reading the file
     */
    public QuorumContractInfoBuilder<TContractType> withBinary(File binaryFile, Charset encoding) throws IOException {
        var binaryFileContent = readFileContent(binaryFile, encoding);
        return withBinary(binaryFileContent);
    }

    /**
     * Specifies the binary of the corresponding smart contract. <br>
     * The binary is optional and only required if the contract is intended to be deployed using the generated wrapper.
     *
     * @param binary hex string representing the contract's binary
     * @return this {@code QuorumContractInfoBuilder}
     */
    public QuorumContractInfoBuilder<TContractType> withBinary(String binary) {
        this.binary = binary;
        return this;
    }

    /**
     * Specifies the nodes that are addressed by transactions created by the smart contract. <br>
     *
     * @param privateFor list of bas64 encoded public keys
     * @return this {@code QuorumContractInfoBuilder}
     */
    public QuorumContractInfoBuilder<TContractType> withPrivateRecipients(List<String> privateFor) {
        this.privateFor = privateFor;
        return this;
    }

    /**
     * Adds an recipient to the list of  nodes that are addressed by transactions created by the smart contract. <br>
     *
     * @param recipient bas64 encoded public key
     * @return this {@code QuorumContractInfoBuilder}
     */
    public QuorumContractInfoBuilder<TContractType> addPrivateRecipient(String recipient){
        if(privateFor == null) privateFor = new ArrayList<>();
        privateFor.add(recipient);
        return this;
    }

    /**
     * Specifies a method as deployment method, i.e. a method that is meant to invoke the smart contract's constructor. <br>
     * <br>
     * This method is a shortcut for calling
     * {@code QuorumMethodInfoBuilder.method(method).name(Constants.DEPLOYMENT_METHOD).specialMethod(true)}
     *
     * @param method method of the smart contract interface
     * @return {@link QuorumMethodInfoBuilder} for the given method.
     */
    public QuorumMethodInfoBuilder deploymentMethod(Method method) {
        return super.method(method)
                .name(Constants.DEPLOYMENT_METHOD)
                .specialMethod(true);
    }

    /**
     * Specifies a method as deployment method, i.e. a method that is meant to invoke the smart contract's constructor. <br>
     * <br>
     * This method is a shortcut for calling
     * {@code QuorumMethodInfoBuilder.method(methodName, parameterTypes).name(Constants.DEPLOYMENT_METHOD).specialMethod(true)}
     *
     * @param methodName     name of the method
     * @param parameterTypes types of the method parameters
     * @return {@link QuorumMethodInfoBuilder} for the given method.
     */
    public QuorumMethodInfoBuilder deploymentMethod(String methodName, Class<?>... parameterTypes) {
        return super.method(methodName, parameterTypes)
                .name(Constants.DEPLOYMENT_METHOD)
                .specialMethod(true);
    }

    /**
     * Creates and initializes a new {@link QuorumMethodInfoBuilder} for the given method.
     *
     * @param method method of the smart contract interface
     * @return new {@link QuorumMethodInfoBuilder} for the given method.
     */
    @Override
    protected QuorumMethodInfoBuilder builderForMethodInternal(Method method) {
        return new QuorumMethodInfoBuilder(this, method);
    }

    /**
     * Creates and initializes a new {@link QuorumEventInfoBuilder} for the given event method.
     *
     * @param method event method of the smart contract interface
     * @return new {@link QuorumEventInfoBuilder} for the given event method.
     */
    @Override
    protected QuorumEventInfoBuilder builderForEventInternal(Method method) {
        return new QuorumEventInfoBuilder(this, method);
    }

    /**
     * Builds the actual {@code QuorumContractInfo}.
     *
     * @return {@code QuorumContractInfo} represented by this builder
     */
    @Override
    protected QuorumContractInfo<TContractType> buildInternal() {
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

        return new QuorumContractInfo<>(identifier, contractType, methodInfos, eventInfos, address, abi, binary, privateFor);
    }


    private String readFileContent(File file, Charset encoding) throws IOException {
        if (encoding == null)
            encoding = StandardCharsets.UTF_8;
        return new String(Files.readAllBytes(file.toPath()), encoding);
    }

    /**
     * Fluent API to build {@code QuorumMethodInfo} objects.
     *
     * @author Matthias Veit
     * @see QuorumContractInfo
     * @see QuorumMethodInfo
     * @since 1.0
     */
    public class QuorumMethodInfoBuilder extends MethodInfoBuilder<QuorumMethodInfoBuilder, QuorumMethodInfo, QuorumContractInfoBuilder<TContractType>, QuorumParameterInfoBuilder> {
        private AbiDefinition abi;

        /**
         * Initializes a new {@code QuorumMethodInfoBuilder} for the given method and assigns the values
         * from the metadata annotation {@link ContractMethod}
         *
         * @param contractInfoBuilder parent builder
         * @param method              smart contract method
         */
        QuorumMethodInfoBuilder(QuorumContractInfoBuilder<TContractType> contractInfoBuilder, Method method) {
            super(contractInfoBuilder, method);
        }

        /**
         * Creates and initializes a new {@link QuorumParameterInfoBuilder} for the given parameter.
         *
         * @param parameter parameter
         * @return new {@link QuorumParameterInfoBuilder} for the given parameter.
         */
        @Override
        protected QuorumParameterInfoBuilder builderForParameterInternal(Parameter parameter) {
            return new QuorumParameterInfoBuilder(this, parameter);
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
         * Creates the {@code QuorumMethodInfo} represented by the builder.
         *
         * @return {@code QuorumMethodInfo} represented by the builder
         */
        @Override
        protected QuorumMethodInfo buildInternal() {
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

            return new QuorumMethodInfo(method, contractMethodName, isReadOnly, isSpecialMethod, parameterInfos, abi, resultTypeConverterClass);
        }
    }

    /**
     * Fluent API to build {@code QuorumParameterInfo} objects.
     *
     * @author Matthias Veit
     * @see QuorumContractInfo
     * @see QuorumParameterInfo
     * @since 1.0
     */
    public class QuorumParameterInfoBuilder extends ParameterInfoBuilder<QuorumParameterInfoBuilder, QuorumParameterInfo, QuorumMethodInfoBuilder> {

        /**
         * Initializes a new {@code QuorumParameterInfoBuilder} for the given parameter and assigns the values
         * from the metadata annotation {@link ContractParameter} and {@link SpecialArgument}
         *
         * @param quorumMethodInfoBuilder parent builder
         * @param parameter                 smart contract method parameter
         */
        QuorumParameterInfoBuilder(QuorumMethodInfoBuilder quorumMethodInfoBuilder, Parameter parameter) {
            super(quorumMethodInfoBuilder, parameter);
        }

        /**
         * Creates the {@code QuorumParameterInfo} represented by the builder.
         *
         * @return {@code QuorumParameterInfo} represented by the builder
         */
        @Override
        protected QuorumParameterInfo buildInternal() {
            String ethereumParameterType;
            if (isSpecialArgument()) {
                ethereumParameterType = null;
            } else {
                ethereumParameterType = this.methodInfoBuilder.getParameterEthereumType(this.parameter);
            }
            return new QuorumParameterInfo(parameter, parameterIndex, ethereumParameterType, typeConverterClass, passAsType, specialArgName);
        }
    }

    /**
     * Fluent API to build {@code QuorumEventInfo} objects.
     *
     * @author Matthias Veit
     * @see QuorumContractInfo
     * @see QuorumEventInfo
     * @since 1.0
     */
    public class QuorumEventInfoBuilder extends EventInfoBuilder<QuorumEventInfoBuilder, QuorumEventInfo, QuorumContractInfoBuilder<TContractType>, QuorumEventFieldInfoBuilder, QuorumEventParameterInfoBuilder> {
        protected AbiDefinition abi;

        /**
         * Initializes a new {@code QuorumEventInfoBuilder} for the given event method and assigns the values
         * from the metadata annotation {@link ContractEvent}
         *
         * @param contractInfoBuilder parent builder
         * @param eventMethod         smart contract event method
         */
        QuorumEventInfoBuilder(QuorumContractInfoBuilder<TContractType> contractInfoBuilder, Method eventMethod) {
            super(contractInfoBuilder, eventMethod);
        }

        /**
         * Creates and initializes a new {@link QuorumEventParameterInfoBuilder} for the given parameter.
         *
         * @param parameter parameter
         * @return new {@link QuorumEventParameterInfoBuilder} for the given parameter.
         */
        @Override
        protected QuorumEventParameterInfoBuilder builderForParameterInternal(Parameter parameter) {
            return new QuorumEventParameterInfoBuilder(this, parameter);
        }

        /**
         * Creates and initializes a new {@link QuorumEventFieldInfoBuilder} for the given field.
         *
         * @param field field
         * @return new {@link QuorumEventFieldInfoBuilder} for the given field.
         */
        @Override
        protected QuorumEventFieldInfoBuilder builderForFieldInternal(Field field) {
            return new QuorumEventFieldInfoBuilder(this, field);
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
         * Creates the {@code QuorumEventInfo} represented by the builder.
         *
         * @return {@code QuorumEventInfo} represented by the builder
         */
        @Override
        protected QuorumEventInfo buildInternal() {
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


            return new QuorumEventInfo(eventName, eventMethod, eventParameterInfos, eventFieldInfos, abi);
        }
    }

    /**
     * Fluent API to build {@code QuorumEventParameterInfo} objects.
     *
     * @author Matthias Veit
     * @see QuorumContractInfo
     * @see QuorumEventParameterInfo
     * @since 1.0
     */
    public class QuorumEventParameterInfoBuilder extends EventParameterInfoBuilder<QuorumEventParameterInfoBuilder, QuorumEventParameterInfo, QuorumEventInfoBuilder> {

        /**
         * Initializes a new {@code QuorumEventParameterInfoBuilder} for the given parameter and assigns the values
         * from the metadata annotation {@link EventParameter} and {@link SpecialArgument}
         *
         * @param eventInfoBuilder parent builder
         * @param eventParameter   smart contract event parameter
         */
        QuorumEventParameterInfoBuilder(QuorumEventInfoBuilder eventInfoBuilder, Parameter eventParameter) {
            super(eventInfoBuilder, eventParameter);
        }

        /**
         * Creates the {@code QuorumEventParameterInfo} represented by the builder.
         *
         * @return {@code QuorumEventParameterInfo} represented by the builder
         */
        @Override
        protected QuorumEventParameterInfo buildInternal() {

            if (StringUtil.isNullOrEmpty(this.argumentName)) {
                throw new IllegalStateException(String.format("Argument name for parameter %s (%s) of event method '%s(...)' is not set.", this.parameterIndex, this.parameter.getType().getName(), this.eventInfoBuilder.getEventMethod().getName()));
            }

            return new QuorumEventParameterInfo(parameter, parameterIndex, argumentName);
        }
    }

    /**
     * Fluent API to build {@code QuorumEventFieldInfo} objects.
     *
     * @author Matthias Veit
     * @see QuorumContractInfo
     * @see QuorumEventFieldInfo
     * @since 1.0
     */
    public class QuorumEventFieldInfoBuilder extends EventFieldInfoBuilder<QuorumEventFieldInfoBuilder, QuorumEventFieldInfo, QuorumEventInfoBuilder> {
        protected AbiDefinition.NamedType solidityType;

        /**
         * Initializes a new {@code QuorumEventFieldInfoBuilder} for the given field and assigns the values
         * from the metadata annotation {@link EventField}
         *
         * @param eventInfoBuilder parent builder
         * @param field            event field
         */
        QuorumEventFieldInfoBuilder(QuorumEventInfoBuilder eventInfoBuilder, Field field) {
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
         * Creates the {@code QuorumEventFieldInfo} represented by the builder.
         *
         * @return {@code QuorumEventFieldInfo} represented by the builder
         */
        @Override
        protected QuorumEventFieldInfo buildInternal() {
            // get Solidity NamedType for this field from parent
            tryGetSolidityTypeFromEventBuilder();

            if (this.solidityType == null) {
                throw new IllegalStateException(String.format("Could not find ABI type definition for event field '%s'. Specified name name: '%s', index: '%s'", field.getName(), Objects.toString(sourceFieldName, "null"), sourceFieldIndex.isPresent() ? sourceFieldIndex.get() : "<not set>"));
            }

            if (StringUtil.isNullOrEmpty(sourceFieldName)) {
                sourceFieldName = null;
            }

            return new QuorumEventFieldInfo(field, sourceFieldName, sourceFieldIndex, Optional.ofNullable(typeConverterClass), solidityType);
        }
    }
}
