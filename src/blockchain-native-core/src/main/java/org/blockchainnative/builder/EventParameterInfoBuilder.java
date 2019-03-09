package org.blockchainnative.builder;

import org.blockchainnative.annotations.EventParameter;
import org.blockchainnative.annotations.SpecialArgument;
import org.blockchainnative.metadata.EventParameterInfo;
import org.blockchainnative.util.ReflectionUtil;
import org.blockchainnative.util.StringUtil;

import java.lang.reflect.Parameter;

/**
 * Fluent API to build {@code EventParameterInfo} objects.
 *
 * @param <TSelf>               Concrete type of the {@link EventParameterInfoBuilder}
 * @param <TEventParameterInfo> Concrete type of the {@link EventParameterInfo} to be created
 * @param <TEventInfoBuilder>   Concrete type of the {@link EventInfoBuilder} used by this builder
 * @author Matthias Veit
 * @see org.blockchainnative.metadata.ContractInfo
 * @see EventParameterInfo
 * @see MethodInfoBuilder
 * @see ParameterInfoBuilder
 * @see EventInfoBuilder
 * @see EventParameterInfoBuilder
 * @see EventFieldInfoBuilder
 * @since 1.0
 */
public abstract class EventParameterInfoBuilder<TSelf extends EventParameterInfoBuilder<TSelf, TEventParameterInfo, TEventInfoBuilder>,
        TEventParameterInfo extends EventParameterInfo,
        TEventInfoBuilder extends EventInfoBuilder> {

    protected final TEventInfoBuilder eventInfoBuilder;
    protected final Parameter parameter;
    protected final int parameterIndex;
    protected TEventParameterInfo eventParameterInfo;
    protected String argumentName;

    /**
     * Initializes a new {@code EventParameterInfoBuilder} for the given parameter and assigns the values
     * from the metadata annotation {@link EventParameter} and {@link SpecialArgument}
     *
     * @param eventInfoBuilder parent builder
     * @param eventParameter   smart contract event parameter
     */
    protected EventParameterInfoBuilder(TEventInfoBuilder eventInfoBuilder, Parameter eventParameter) {
        this.eventInfoBuilder = eventInfoBuilder;
        this.parameter = eventParameter;
        this.parameterIndex = ReflectionUtil.getParameterIndex(eventInfoBuilder.eventMethod, eventParameter);

        if (parameterIndex == -1) {
            throw new IllegalArgumentException(String.format("Parameter not found on event method '%s(...)'.", eventInfoBuilder.eventMethod.getName()));
        }

        parseAnnotations();
    }

    private void parseAnnotations() {
        // parse values provided via annotation values
        var parameterAnnotation = parameter.getAnnotation(EventParameter.class);
        if (parameterAnnotation != null) {
            if (!StringUtil.isNullOrEmpty(parameterAnnotation.value())) {
                this.argumentName = parameterAnnotation.value();
            }
        } else {
            var specialArgAnnotation = parameter.getAnnotation(SpecialArgument.class);
            if (specialArgAnnotation != null) {
                if (!StringUtil.isNullOrEmpty(specialArgAnnotation.value())) {
                    this.argumentName = specialArgAnnotation.value();
                }
            }
        }
    }

    /** Controls whether or not the parameter targeted by this {@code ParameterInfo} is marked as special argument to the underlying provider. <br>
     *  How this parameter is interpreted is up to provider. <br>
     *  Initial value is taken from {@link SpecialArgument#value()}
     *
     * @param name name of the special argument or null if the parameter should not be treated as special argument
     * @return {@code ParameterInfoBuilder}
     */

    /**
     * Sets the name of the event parameter. <br>
     * Since all event parameter are by default special arguments, the initial value is taken either from {@link EventParameter} or {@link SpecialArgument}
     *
     * @param name name of the event parameter.
     * @return this {@code EventParameterInfoBuilder}
     */
    public TSelf withArgumentName(String name) {
        this.argumentName = name;
        return self();
    }

    /**
     * Returns itself as the generic parameter {@code TSelf}. <br>
     * Allows sub types of {@code EventParameterInfoBuilder} to return the correct type from the fluent methods like {@link EventParameterInfoBuilder#withArgumentName(String)}.
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
        return eventParameterInfo != null;
    }

    /**
     * Returns the {@code EventParameterInfo} object declared through the fluent API. <br>
     * Must only be called after {@link EventParameterInfoBuilder#build()}.
     *
     * @return {@code EventParameterInfo} object represented by the builder
     * @throws IllegalStateException in case the {@code build()} has not been called before.
     */
    public TEventParameterInfo getEventParameterInfo() {
        if (this.eventParameterInfo == null) {
            throw new IllegalStateException("build() must be called before retrieving the EventParameterInfo!");
        }
        return this.eventParameterInfo;
    }

    /**
     * Creates the {@link EventParameterInfo} object declared through the fluent API. <br>
     * Must only be called once.
     *
     * @return {@code EventParameterInfo} object represented by the builder
     * @throws IllegalStateException in case the {@code build()} has been called before.
     */
    public final TEventInfoBuilder build() {
        if (this.eventParameterInfo != null) {
            throw new IllegalStateException("build() must not be called more than once!");
        }
        this.eventParameterInfo = buildInternal();
        return this.eventInfoBuilder;
    }

    /**
     * Creates the concrete {@code EventParameterInfo} represented by the builder.
     *
     * @return {@code EventParameterInfo} represented by the builder
     */
    protected abstract TEventParameterInfo buildInternal();
}
