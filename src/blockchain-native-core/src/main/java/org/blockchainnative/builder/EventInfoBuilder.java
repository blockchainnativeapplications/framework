package org.blockchainnative.builder;

import io.reactivex.Observable;
import org.blockchainnative.annotations.ContractEvent;
import org.blockchainnative.metadata.EventInfo;
import org.blockchainnative.util.ReflectionUtil;
import org.blockchainnative.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Fluent API to build {@code EventInfo} objects.
 *
 * @param <TSelf>                      Concrete type of the {@link EventInfoBuilder}
 * @param <TEventInfo>                 Concrete type of the {@link EventInfo} to be created
 * @param <TContractInfoBuilder>       Concrete type of the {@link ContractInfoBuilder} used by this builder
 * @param <TEventFieldInfoBuilder>     Concrete type of the {@link EventFieldInfoBuilder} used by this builder
 * @param <TEventParameterInfoBuilder> Concrete type of the {@link EventParameterInfoBuilder} used by this builder
 * @author Matthias Veit
 * @since 1.0
 */
public abstract class EventInfoBuilder<TSelf extends EventInfoBuilder<TSelf, TEventInfo, TContractInfoBuilder, TEventFieldInfoBuilder, TEventParameterInfoBuilder>,
        TEventInfo extends EventInfo,
        TContractInfoBuilder extends ContractInfoBuilder,
        TEventFieldInfoBuilder extends EventFieldInfoBuilder,
        TEventParameterInfoBuilder extends EventParameterInfoBuilder> {

    protected final TContractInfoBuilder contractInfoBuilder;
    protected final Method eventMethod;
    protected final SortedMap<Parameter, TEventParameterInfoBuilder> eventParameterInfoBuilders;
    protected final Map<Field, TEventFieldInfoBuilder> eventFieldInfoBuilders;
    protected String eventName;
    protected TEventInfo eventInfo;

    /**
     * Initializes a new {@code EventInfoBuilder} for the given event method and assigns the values
     * from the metadata annotation {@link org.blockchainnative.annotations.ContractEvent}
     *
     * @param contractInfoBuilder parent builder
     * @param eventMethod         smart contract event method
     */
    protected EventInfoBuilder(TContractInfoBuilder contractInfoBuilder, Method eventMethod) {
        if (contractInfoBuilder == null) throw new IllegalArgumentException("ContractInfoBuilder must not be null!");
        if (eventMethod == null) throw new IllegalArgumentException("Method must not be null!");

        this.contractInfoBuilder = contractInfoBuilder;
        this.eventMethod = eventMethod;
        this.eventParameterInfoBuilders = new TreeMap<>(new ParameterByIndexComparator(eventMethod));
        // pre-populate ParameterBuilders
        for (var parameter : this.eventMethod.getParameters()) {
            this.eventParameterInfoBuilders.put(parameter, this.builderForParameterInternal(parameter));
        }
        this.eventFieldInfoBuilders = new HashMap<>();

        for (var field : ReflectionUtil.getEventType(eventMethod).getDeclaredFields()) {
            this.eventFieldInfoBuilders.put(field, this.builderForFieldInternal(field));
        }
        parseAnnotations();
    }

    private void parseAnnotations() {
        // parse values provided via annotation values

        var eventAnnotation = eventMethod.getAnnotation(ContractEvent.class);
        if (eventAnnotation != null && !StringUtil.isNullOrEmpty(eventAnnotation.value())) {
            this.eventName = eventAnnotation.value();
        } else {
            this.eventName = eventMethod.getName();
        }
    }

    /**
     * Sets the name of the smart contract event targeted by this {@code EventInfo} <br>
     * Initial value is taken from {@link org.blockchainnative.annotations.ContractEvent#value()}
     *
     * @param name name of the smart contract event targeted by this {@code EventInfo}
     * @return this {@code EventInfoBuilder}
     */
    public TSelf name(String name) {
        this.eventName = name;
        return self();
    }

    /**
     * Creates or returns a preexisting {@link EventFieldInfoBuilder} for a field of the event type used by this {@code EventInfo}.
     *
     * @param sourceFieldName name of a field of the event type used by this {@code EventInfo}.
     * @return {@link EventFieldInfoBuilder} for the specified field
     */
    public TEventFieldInfoBuilder eventField(String sourceFieldName) {
        if (StringUtil.isNullOrEmpty(sourceFieldName))
            throw new IllegalArgumentException("sourceFieldName must not be null or empty");

        Field field;
        var eventType = ReflectionUtil.getEventType(eventMethod);

        try {
            field = eventType.getField(sourceFieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(
                    String.format("Could not find field '%s' on event object of type '%s'",
                            sourceFieldName,
                            eventType.getName()));
        }

        return getOrCreateEventFieldInfoBuilder(field);
    }

    /**
     * Creates or returns a preexisting {@link EventParameterInfoBuilder} for a parameter of the event method targeted by this {@code EventInfo}
     *
     * @param parameter parameter of the event method targeted by this {@code EventInfo}
     * @return {@link EventParameterInfoBuilder} for the specified parameter
     */
    public TEventParameterInfoBuilder eventParameter(Parameter parameter) {
        var parameters = eventMethod.getParameters();
        var parameterIndex = -1;
        for (var i = 0; i < parameters.length; i++) {
            if (parameters[i].equals(parameter)) {
                parameterIndex = i;
                break;
            }
        }

        if (parameterIndex == -1) {
            throw new IllegalArgumentException(
                    String.format("Could not find parameter '%s <%s>' on event method '%s(...)' of type '%s'",
                            parameter.getName(),
                            parameter.getType().getSimpleName(),
                            eventMethod.getName(),
                            contractInfoBuilder.contractType.getTypeName()));
        }

        return getOrCreateEventParameterInfoBuilder(parameter);
    }

    /**
     * Creates or returns a preexisting {@link EventParameterInfoBuilder} for a parameter of the event method targeted by this {@code EventInfo}
     *
     * @param i index of a parameter of the method targeted by this {@code EventInfo}
     * @return {@link EventParameterInfoBuilder} for the specified parameter
     */
    public TEventParameterInfoBuilder eventParameter(int i) {
        if (i < 0)
            throw new IllegalArgumentException("Parameter index needs to be greater than zero");

        if (i >= eventMethod.getParameters().length)
            throw new IllegalArgumentException(
                    String.format("Could not provide builder for field at position %s on event method '%s(...)' of type '%s since it only takes %s parameters.'",
                            i,
                            eventMethod.getName(),
                            contractInfoBuilder.contractType.getTypeName(),
                            eventMethod.getParameterCount()));


        var parameter = eventMethod.getParameters()[i];

        return getOrCreateEventParameterInfoBuilder(parameter);
    }

    /**
     * Creates or returns a preexisting {@link EventParameterInfoBuilder} for a parameter of the event method targeted by this {@code EventInfo}
     *
     * @param parameter parameter of the event method targeted by this {@code EventInfo}
     * @return {@link EventParameterInfoBuilder} for the specified parameter
     */
    protected TEventParameterInfoBuilder getOrCreateEventParameterInfoBuilder(Parameter parameter) {
        TEventParameterInfoBuilder parameterInfoBuilder;
        if (eventParameterInfoBuilders.containsKey(parameter)) {
            parameterInfoBuilder = eventParameterInfoBuilders.get(parameter);
        } else {
            parameterInfoBuilder = builderForParameterInternal(parameter);
            this.eventParameterInfoBuilders.put(parameter, parameterInfoBuilder);
        }

        return parameterInfoBuilder;
    }

    /**
     * Creates or returns a preexisting {@link EventFieldInfoBuilder} for a field of the event type used by this {@code EventInfo}.
     *
     * @param field field of the event type used by this {@code EventInfo}.
     * @return {@link EventFieldInfoBuilder} for the specified field
     */
    protected TEventFieldInfoBuilder getOrCreateEventFieldInfoBuilder(Field field) {
        TEventFieldInfoBuilder fieldInfoBuilder;
        if (eventFieldInfoBuilders.containsKey(field)) {
            fieldInfoBuilder = eventFieldInfoBuilders.get(field);
        } else {
            fieldInfoBuilder = builderForFieldInternal(field);
            this.eventFieldInfoBuilders.put(field, fieldInfoBuilder);
        }

        return fieldInfoBuilder;
    }

    /**
     * Returns method targeted by this {@code EventInfoBuilder}
     *
     * @return method targeted by this {@code EventInfoBuilder}
     */
    public Method getEventMethod() {
        return eventMethod;
    }

    /**
     * Returns itself as the generic parameter {@code TSelf}. <br>
     * Allows sub types of {@code EventInfoBuilder} to return the correct type from the fluent methods like {@link EventInfoBuilder#name(String)}.
     *
     * @return {@code this} casted to {@code TSelf}
     */
    @SuppressWarnings("unchecked")
    protected TSelf self() {
        return (TSelf) this;
    }

    /**
     * Returns the {@code EventInfo} object declared through the fluent API. <br>
     * Must only be called after {@link EventInfoBuilder#build()}.
     *
     * @return {@code EventInfo} object represented by the builder
     * @throws IllegalStateException in case the {@code build()} has not been called before.
     */
    public TEventInfo getEventInfo() {
        if (this.eventInfo == null) {
            throw new IllegalStateException("build() must be called before retrieving the EventInfo!");
        }
        return this.eventInfo;
    }

    /**
     * Returns if the {@code EventInfo} object represented by the builder has already been built or not.
     *
     * @return boolean value indicating if the {@code EventInfo} object represented by the builder has already been built or not
     */
    public boolean hasBeenBuilt() {
        return this.eventInfo != null;
    }

    /**
     * Creates the {@code EventInfo} object declared through the fluent API. <br>
     * Must only be called once.
     *
     * @return {@code EventInfo} object represented by the builder
     * @throws IllegalStateException in case the {@code build()} has been called before.
     */
    public final TContractInfoBuilder build() {
        if (this.eventInfo != null) {
            throw new IllegalStateException("build() must not be called more than once!");
        }

        if (!Observable.class.equals(eventMethod.getReturnType())) {
            throw new IllegalStateException(String.format("Unexpected return type of event method '%s(...)', events need to return '%s' with the generic type optionally wrapped in Event<>.", eventMethod.getName(), Observable.class.getName()));
        }

        this.eventInfo = buildInternal();
        return this.contractInfoBuilder;
    }

    /**
     * Creates and initializes a new {@link EventParameterInfoBuilder} for the given parameter.
     *
     * @param parameter parameter
     * @return new {@link EventParameterInfoBuilder} for the given parameter.
     */
    protected abstract TEventParameterInfoBuilder builderForParameterInternal(Parameter parameter);

    /**
     * Creates and initializes a new {@link EventFieldInfoBuilder} for the given field.
     *
     * @param field field
     * @return new {@link EventFieldInfoBuilder} for the given field.
     */
    protected abstract TEventFieldInfoBuilder builderForFieldInternal(Field field);

    /**
     * Creates the concrete {@code EventInfo} represented by the builder.
     *
     * @return {@code EventInfo} represented by the builder
     */
    protected abstract TEventInfo buildInternal();
}
