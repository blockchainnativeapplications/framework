package org.blockchainnative.builder;

import org.blockchainnative.annotations.ContractEvent;
import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.annotations.SmartContract;
import org.blockchainnative.metadata.ContractInfo;
import org.blockchainnative.metadata.EventInfo;
import org.blockchainnative.metadata.MethodInfo;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fluent API to build {@code ContractInfo} objects.
 *
 * @param <TSelf>              Concrete type of the {@link ContractInfoBuilder}
 * @param <TContractType>      Java interface type representing the smart contract
 * @param <TContractInfo>      Concrete type of the {@link ContractInfo} to be created
 * @param <TMethodInfoBuilder> Concrete type of the {@link MethodInfoBuilder} used by this builder
 * @param <TMethodInfo>        Concrete type of the {@link MethodInfo} objects used by {@code ContractInfo}
 * @param <TEventInfoBuilder>  Concrete type of the {@link EventInfoBuilder} by this builder
 * @param <TEventInfo>         Concrete type of the {@link EventInfo} objects used by {@code ContractInfo}
 * @author Matthias Veit
 * @see ContractInfo
 * @see MethodInfoBuilder
 * @see ParameterInfoBuilder
 * @see EventInfoBuilder
 * @see EventParameterInfoBuilder
 * @see EventFieldInfoBuilder
 * @since 1.0
 */
public abstract class ContractInfoBuilder<TSelf extends ContractInfoBuilder<TSelf, TContractType, TContractInfo, TMethodInfoBuilder, TMethodInfo, TEventInfoBuilder, TEventInfo>,
        TContractType,
        TContractInfo extends ContractInfo<TContractType, TMethodInfo, TEventInfo>,
        TMethodInfoBuilder extends MethodInfoBuilder,
        TMethodInfo extends MethodInfo,
        TEventInfoBuilder extends EventInfoBuilder,
        TEventInfo extends EventInfo<?, ?>> {
    protected String identifier;
    protected Class<TContractType> contractType;
    protected Map<Method, TMethodInfoBuilder> methodInfoBuilders;
    protected Map<Method, TEventInfoBuilder> eventInfoBuilders;
    protected TContractInfo contractInfo;

    /**
     * Initializes a new ContractInfoBuilder for the given contractType and creates builders
     * for each method annotated with {@link ContractMethod} and {@link ContractEvent}.
     *
     * @param contractType Java interface representing the smart contract
     */
    public ContractInfoBuilder(Class<TContractType> contractType) {
        this.contractType = contractType;
        this.methodInfoBuilders = new HashMap<>();
        this.eventInfoBuilders = new HashMap<>();

        parseAnnotations();
    }

    private void parseAnnotations() {
        var smartContractAnnotation = this.contractType.getAnnotation(SmartContract.class);
        if (smartContractAnnotation != null) {
            this.identifier = smartContractAnnotation.value();
        }

        for (var method : this.contractType.getMethods()) {
            var contractMethodAnnotation = method.getAnnotation(ContractMethod.class);
            if (contractMethodAnnotation != null) {
                this.methodInfoBuilders.put(method, builderForMethodInternal(method));
            }

            var contractEventAnnotation = method.getAnnotation(ContractEvent.class);
            if (contractEventAnnotation != null) {
                this.eventInfoBuilders.put(method, builderForEventInternal(method));
            }
        }
    }

    /**
     * Sets the identifier of the {@code ContractInfo}. <br>
     * Initial value is taken from {@link SmartContract#value()}
     * If no identifier is set when {@link ContractInfoBuilder#build()} is called, a random id will be generated.
     *
     * @param identifier identifier to be used
     * @return {@code ContractInfoBuilder}
     */
    public TSelf withIdentifier(String identifier) {
        this.identifier = identifier;

        return self();
    }

    /**
     * Creates or returns a preexisting {@link MethodInfoBuilder} for a method of the smart contract interface.
     *
     * @param methodName     name of the method
     * @param parameterTypes types of the method parameters
     * @return {@link MethodInfoBuilder} for the specified method
     */
    public TMethodInfoBuilder method(String methodName, Class<?>... parameterTypes) {
        var method = ensureMethodBelongsToContractType(methodName, parameterTypes);

        return getOrCreateMethodInfoBuilder(method);
    }

    /**
     * Creates or returns a preexisting {@link MethodInfoBuilder} for a method of the smart contract interface.
     *
     * @param method method of the smart contract interface
     * @return {@link MethodInfoBuilder} for the specified method
     */
    public TMethodInfoBuilder method(Method method) {
        ensureMethodBelongsToContractType(method.getName(), method.getParameterTypes());

        return getOrCreateMethodInfoBuilder(method);
    }

    /**
     * Creates or returns a preexisting {@link EventInfoBuilder} for a event method of the smart contract interface.
     *
     * @param methodName     name of the event method
     * @param parameterTypes types of the event method parameters
     * @return {@link EventInfoBuilder} for the specified event method
     */
    public TEventInfoBuilder event(String methodName, Class<?>... parameterTypes) {
        var method = ensureMethodBelongsToContractType(methodName, parameterTypes);

        return getOrCreateEventInfoBuilder(method);
    }

    /**
     * Creates or returns a preexisting {@link EventInfoBuilder} for a event method of the smart contract interface.
     *
     * @param method event method of the smart contract interface
     * @return {@link EventInfoBuilder} for the specified event method
     */
    public TEventInfoBuilder event(Method method) {
        ensureMethodBelongsToContractType(method.getName(), method.getParameterTypes());

        return getOrCreateEventInfoBuilder(method);
    }

    /**
     * Ensures that a specified method is declared on the smart contract interface (or one of its super types)
     *
     * @param methodName     name of the method
     * @param parameterTypes types of the method parameters
     * @return {@code Method} object representing the defined method
     * @throws IllegalArgumentException in case the method could not be found on the smart contract interface type.
     */
    protected Method ensureMethodBelongsToContractType(String methodName, Class<?>... parameterTypes) {
        try {
            return this.contractType.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    String.format("Could not find method '%s(%s)'  on type '%s'",
                            methodName,
                            Arrays.stream(parameterTypes).map(Class::getSimpleName).collect(Collectors.joining(",")),
                            contractType.getTypeName()),
                    e);
        }
    }


    /**
     * Creates or returns a preexisting {@link MethodInfoBuilder} for a method of the smart contract interface.
     *
     * @param method method of the smart contract interface
     * @return {@link MethodInfoBuilder} for the specified method
     */
    protected TMethodInfoBuilder getOrCreateMethodInfoBuilder(Method method) {
        if (this.methodInfoBuilders.containsKey(method))
            return this.methodInfoBuilders.get(method);

        var builder = builderForMethodInternal(method);
        this.methodInfoBuilders.put(method, builder);

        return builder;
    }

    /**
     * Creates or returns a preexisting {@link EventInfoBuilder} for a event method of the smart contract interface.
     *
     * @param method event method of the smart contract interface
     * @return {@link EventInfoBuilder} for the specified event method
     */
    protected TEventInfoBuilder getOrCreateEventInfoBuilder(Method method) {
        if (this.eventInfoBuilders.containsKey(method))
            return this.eventInfoBuilders.get(method);

        var builder = builderForEventInternal(method);
        this.eventInfoBuilders.put(method, builder);

        return builder;
    }

    /**
     * Creates a random identifier using {@code UUID.randomUUID()}
     *
     * @return random identifier
     */
    protected static String getRandomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Returns itself as the generic parameter {@code TSelf}. <br>
     * Allows sub types of {@code ContractInfoBuilder} to return the correct type from the fluent methods like {@link ContractInfoBuilder#withIdentifier(String)}.
     *
     * @return {@code this} casted to {@code TSelf}
     */
    @SuppressWarnings("unchecked")
    protected TSelf self() {
        return (TSelf) this;
    }

    /**
     * Creates the {@link ContractInfo} object declared through the fluent API. <br>
     * Must only be called once.
     *
     * @return {@code ContractInfo} object represented by the builder
     * @throws IllegalStateException in case the {@code build()} has been called before.
     */
    public final TContractInfo build() {
        if (this.contractInfo != null) {
            throw new IllegalStateException("build() must not be called more than once");
        }

        this.contractInfo = buildInternal();
        return this.contractInfo;
    }

    /**
     * Creates and initializes a new {@link MethodInfoBuilder} for the given method.
     *
     * @param method method of the smart contract interface
     * @return new {@link MethodInfoBuilder} for the given method.
     */
    protected abstract TMethodInfoBuilder builderForMethodInternal(Method method);

    /**
     * Creates and initializes a new {@link EventInfoBuilder} for the given event method.
     *
     * @param method event method of the smart contract interface
     * @return new {@link EventInfoBuilder} for the given event method.
     */
    protected abstract TEventInfoBuilder builderForEventInternal(Method method);

    /**
     * Creates the concrete {@code ContractInfo} represented by the builder.
     *
     * @return {@code ContractInfo} represented by the builder
     */
    protected abstract TContractInfo buildInternal();
}
