package org.blockchainnative.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Holds additional information about a smart contract event. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.builder.ContractInfoBuilder} and its subtypes to construct
 * instances of this class.
 *
 * @param <TEventFieldInfo>     Concrete type of the {@link EventFieldInfo} objects used by {@code EventInfo}.
 * @param <TEventParameterInfo> Concrete type of the {@link EventParameterInfo} objects used by {@code EventInfo}.
 * @author Matthias Veit
 * @see org.blockchainnative.builder.ContractInfoBuilder
 * @see ContractInfo
 * @see org.blockchainnative.ContractWrapperGenerator
 * @since 1.0
 */
public class EventInfo<TEventFieldInfo extends EventFieldInfo, TEventParameterInfo extends EventParameterInfo> {
    protected final String name;
    protected final Method method;
    protected final Class<?> eventType;
    protected final List<TEventParameterInfo> eventParameterInfos;
    protected final List<TEventFieldInfo> eventFieldInfos;


    /**
     * Constructs a new {@code EventInfo}
     *
     * @param name                name of the corresponding smart contract event.
     * @param method              method on the smart contract interface representing the event.
     * @param eventParameterInfos {@code EventParameterInfo} objects
     * @param eventFieldInfos     {@code EventFieldInfo} objects
     */
    public EventInfo(String name, Method method, Collection<TEventParameterInfo> eventParameterInfos, Collection<TEventFieldInfo> eventFieldInfos) {
        this.name = name;
        this.method = method;
        this.eventType = ReflectionUtil.getEventType(method);

        this.eventParameterInfos = new ArrayList<>();
        if (eventParameterInfos != null) {
            this.eventParameterInfos.addAll(eventParameterInfos);
        }

        this.eventFieldInfos = new ArrayList<>();
        if (eventFieldInfos != null) {
            this.eventFieldInfos.addAll(eventFieldInfos);
        }
    }

    /**
     * Returns the name of the smart contract event.
     *
     * @return name of the smart contract event.
     */
    public String getEventName() {
        return name;
    }

    /**
     * Returns the event type used to encapsulate the events fields.
     *
     * @return event type used to encapsulate the events fields.
     */
    public Class<?> getEventType() {
        return eventType;
    }

    /**
     * Returns the event method of the corresponding contract interface.
     *
     * @return event method of the corresponding contract interface.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns all {@code EventParameterInfo} objects registered with this event.
     *
     * @return List of all {@code EventParameterInfo} objects registered with this event.
     */
    public List<TEventParameterInfo> getEventParameterInfos() {
        return eventParameterInfos;
    }

    /**
     * Returns all {@code EventFieldInfo} objects registered with this event.
     *
     * @return List of all {@code EventFieldInfo} objects registered with this event.
     */
    public List<TEventFieldInfo> getEventFieldInfos() {
        return eventFieldInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof EventInfo)) return false;

        EventInfo<?, ?> eventInfo = (EventInfo<?, ?>) o;

        return new EqualsBuilder()
                .append(name, eventInfo.name)
                .append(eventType, eventInfo.eventType)
                .append(eventParameterInfos, eventInfo.eventParameterInfos)
                .append(eventFieldInfos, eventInfo.eventFieldInfos)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(eventType)
                .append(eventParameterInfos)
                .append(eventFieldInfos)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("eventType", eventType)
                .toString();
    }
}
