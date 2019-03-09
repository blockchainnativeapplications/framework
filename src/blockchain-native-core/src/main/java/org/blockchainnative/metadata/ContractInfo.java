package org.blockchainnative.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Holds all relevant information about smart contract interfaces in order to allow implementations of
 * {@link org.blockchainnative.ContractWrapperGenerator} to generate a functioning wrapper class. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.builder.ContractInfoBuilder} and its subtypes to construct
 * instances of this class.
 *
 * @param <TContractType> smart contract interface type
 * @param <TMethodInfo>   Concrete type of the {@link MethodInfo} objects used by {@code ContractInfo}
 * @param <TEventInfo>    Concrete type of the {@link EventInfo} objects used by {@code ContractInfo}
 * @author Matthias Veit
 * @see org.blockchainnative.builder.ContractInfoBuilder
 * @see org.blockchainnative.ContractWrapperGenerator
 * @since 1.0
 */
public class ContractInfo<TContractType, TMethodInfo extends MethodInfo, TEventInfo extends EventInfo<?, ?>> {
    private final String identifier;
    private final Class<TContractType> contractClass;
    private final Map<Method, TMethodInfo> methods;
    private final Map<String, TEventInfo> events;

    /**
     * Constructs a new instance of a contract info.
     *
     * @param identifier    contract identifier
     * @param contractClass java interface representing the smart contract
     * @param methodInfos   method info objects
     * @param eventInfos    event info objects
     */
    public ContractInfo(String identifier, Class<TContractType> contractClass, Map<Method, TMethodInfo> methodInfos, Map<String, TEventInfo> eventInfos) {
        this(identifier, contractClass, methodInfos.values(), eventInfos.values());
    }

    /**
     * Constructs a new instance of a contract info.
     *
     * @param identifier    contract identifier
     * @param contractClass java interface representing the smart contract
     * @param methodInfos   method info objects
     * @param eventInfos    event info objects
     */
    public ContractInfo(String identifier, Class<TContractType> contractClass, Collection<TMethodInfo> methodInfos, Collection<TEventInfo> eventInfos) {
        this.identifier = identifier;
        this.contractClass = contractClass;
        this.methods = new HashMap<>();
        if (methodInfos != null) {
            methodInfos.forEach(methodInfo -> this.methods.put(methodInfo.getMethod(), methodInfo));
        }
        this.events = new HashMap<>();
        if (eventInfos != null) {
            eventInfos.forEach(eventInfo -> this.events.put(eventInfo.getEventName(), eventInfo));
        }
    }

    /**
     * Returns the class object of the smart contract wrapper interface.
     *
     * @return Class object of the smart contract wrapper interface.
     */
    public Class<TContractType> getContractClass() {
        return contractClass;
    }

    /**
     * Returns the identifier registered with this {@code ContractInfo}
     *
     * @return identifier of the {@code ContractInfo}
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets all {@code MethodInfo} objects registered on the contract info.
     *
     * @return unmodifiable map of contract methods with the {@code Method} as key and the {@code MethodInfo} object as value.
     */
    public Map<Method, TMethodInfo> getMethodInfos() {
        return Collections.unmodifiableMap(methods);
    }

    /**
     * Gets the {@code MethodInfo} object of an event represented by the corresponding method on the smart contract interface.
     *
     * @param method method for which the {@code MethodInfo} should be retrieved
     * @return {@code MethodInfo} object or null if the given method is not registered as contract method on the contract info.
     */
    public TMethodInfo getMethodInfo(Method method) {
        return this.methods.get(method);
    }

    /**
     * Gets all {@code EventInfo} objects registered on the contract info.
     *
     * @return unmodifiable map of events with the event's name as key and the {@code EventInfo} object as value.
     */
    public Map<String, TEventInfo> getEventInfos() {
        return Collections.unmodifiableMap(events);
    }

    /**
     * Gets the {@code EventInfo} object of an event represented by its name.
     *
     * @param eventName name of the event for which the {@code EventInfo} should be retrieved
     * @return {@code EventInfo} object or null if no event with the given name is registered on the contract info.
     */
    public TEventInfo getEventInfo(String eventName) {
        return this.events.get(eventName);
    }

    /**
     * Gets the {@code EventInfo} object of an event represented by the corresponding method on the smart contract interface.
     *
     * @param eventMethod method for which the {@code EventInfo} should be retrieved
     * @return {@code EventInfo} object or null if the given method is not registered as event on the contract info.
     */
    public TEventInfo getEventInfo(Method eventMethod) {
        return this.events.values().stream().filter(eventInfo -> eventInfo.getMethod().equals(eventMethod)).findAny().orElse(null);
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("identifier", identifier)
                .append("contractClass", contractClass)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ContractInfo)) return false;

        ContractInfo<?, ?, ?> that = (ContractInfo<?, ?, ?>) o;

        return new EqualsBuilder()
                .append(identifier, that.identifier)
                .append(contractClass, that.contractClass)
                .append(methods, that.methods)
                .append(events, that.events)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(identifier)
                .append(contractClass)
                .append(methods)
                .append(events)
                .toHashCode();
    }
}
