package org.blockchainnative.builder;

import org.blockchainnative.convert.NoOpTypeConverter;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.annotations.EventField;
import org.blockchainnative.metadata.EventFieldInfo;
import org.blockchainnative.util.StringUtil;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Fluent API to build {@code EventFieldInfo} objects.
 *
 * @param <TSelf>             Concrete type of the {@link EventFieldInfoBuilder}
 * @param <TEventFieldInfo>   Concrete type of the {@link EventFieldInfo} to be created
 * @param <TEventInfoBuilder> Concrete type of the {@link EventInfoBuilder} used by this builder
 * @author Matthias Veit
 * @see org.blockchainnative.metadata.ContractInfo
 * @see EventFieldInfo
 * @see MethodInfoBuilder
 * @see ParameterInfoBuilder
 * @see EventInfoBuilder
 * @see EventParameterInfoBuilder
 * @see EventFieldInfoBuilder
 * @since 1.0
 */
public abstract class EventFieldInfoBuilder<TSelf extends EventFieldInfoBuilder<TSelf, TEventFieldInfo, TEventInfoBuilder>,
        TEventFieldInfo extends EventFieldInfo,
        TEventInfoBuilder extends EventInfoBuilder> {

    protected final TEventInfoBuilder eventInfoBuilder;
    protected final Field field;
    protected String sourceFieldName;
    protected Optional<Integer> sourceFieldIndex;
    protected Class<? extends TypeConverter<?, ?>> typeConverterClass;

    protected TEventFieldInfo eventFieldInfo;

    /**
     * Initializes a new {@code EventFieldInfoBuilder} for the given field and assigns the values
     * from the metadata annotation {@link EventField}
     *
     * @param eventInfoBuilder parent builder
     * @param field            event field
     */
    protected EventFieldInfoBuilder(TEventInfoBuilder eventInfoBuilder, Field field) {
        this.eventInfoBuilder = eventInfoBuilder;
        this.field = field;
        this.sourceFieldIndex = Optional.empty();

        parseAnnotations();
    }

    private void parseAnnotations() {
        var eventFieldAnnotation = field.getAnnotation(EventField.class);
        if (eventFieldAnnotation != null) {
            if (StringUtil.isNullOrEmpty(eventFieldAnnotation.value())) {
                this.sourceFieldName = field.getName();
            } else {
                this.sourceFieldName = eventFieldAnnotation.value();
            }
            var annotationIndex = eventFieldAnnotation.index();
            if (annotationIndex > -1) {
                this.sourceFieldIndex = Optional.of(annotationIndex);
            } else {
                this.sourceFieldIndex = Optional.empty();
            }

            var converterClass = eventFieldAnnotation.useTypeConverter();
            if (converterClass != null && converterClass != NoOpTypeConverter.class) {
                this.typeConverterClass = converterClass;
            }
        }
        if (Optional.empty().equals(this.sourceFieldIndex) && this.sourceFieldName == null) {
            this.sourceFieldName = field.getName();
        }
    }

    /**
     * Defines the name of the field as it is called in corresponding smart contract event. <br>
     * If the blockchain's implementation of events does not support multiple or named fields, this value is meaningless. <br>
     * Initial value is taken from {@link EventField#value()} <br>
     * <br>
     * Either the name or the index of the event field should be set, but not both!
     *
     * @param name name of the targeted event field
     * @return this {@code EventFieldInfoBuilder}
     */
    public TSelf sourceFieldName(String name) {
        this.sourceFieldName = name;
        return self();
    }

    /**
     * Defines the index of the targeted field of the corresponding smart contract event. <br>
     * If the blockchain's implementation of events does not support multiple fields, this value is meaningless. <br>
     * Initial value is taken from {@link EventField#index()} <br>
     * <br>
     * Either the name or the index of the event field should be set, but not both!
     *
     * @param index index of the targeted event field
     * @return this {@code EventFieldInfoBuilder}
     */
    public TSelf atIndex(int index) {
        this.sourceFieldIndex = Optional.of(index);
        return self();
    }

    /**
     * Sets the {@code TypeConverter} to be used to convert smart contract event field value to the declared type of the field in the event object. <br>
     * Initial value is taken from {@link EventField#useTypeConverter()}
     *
     * @param typeConverterClass class of the {@code TypeConverter} to be used to convert the event field value.
     * @return this {@code EventFieldInfoBuilder}
     */
    public TSelf useTypeConverter(Class<? extends TypeConverter<?, ?>> typeConverterClass) {
        this.typeConverterClass = typeConverterClass;
        return self();
    }

    /**
     * Returns itself as the generic parameter {@code TSelf}. <br>
     * Allows sub types of {@code EventFieldInfoBuilder} to return the correct type from the fluent methods like {@link EventFieldInfoBuilder#useTypeConverter(Class)}.
     *
     * @return {@code this} casted to {@code TSelf}
     */
    @SuppressWarnings("unchecked")
    protected TSelf self() {
        return (TSelf) this;
    }

    /**
     * Returns if the {@code EventFieldInfo} object represented by the builder has already been built or not.
     *
     * @return boolean value indicating if the {@code EventFieldInfo} object represented by the builder has already been built or not
     */
    public boolean hasBeenBuilt() {
        return eventFieldInfo != null;
    }

    /**
     * Returns the {@code EventFieldInfo} object declared through the fluent API. <br>
     * Must only be called after {@link EventFieldInfoBuilder#build()}.
     *
     * @return {@code EventFieldInfo} object represented by the builder
     * @throws IllegalStateException in case the {@code build()} has not been called before.
     */
    public TEventFieldInfo getEventFieldInfo() {
        if (this.eventFieldInfo == null) {
            throw new IllegalStateException("build() must be called before retrieving the EventParameterInfo!");
        }
        return this.eventFieldInfo;
    }

    /**
     * Creates the {@link EventFieldInfo} object declared through the fluent API. <br>
     * Must only be called once.
     *
     * @return {@code EventFieldInfo} object represented by the builder
     * @throws IllegalStateException in case the {@code build()} has been called before.
     */
    public final TEventInfoBuilder build() {
        if (this.eventFieldInfo != null) {
            throw new IllegalStateException("build() must not be called more than once!");
        }
        this.eventFieldInfo = buildInternal();
        return this.eventInfoBuilder;
    }

    /**
     * Creates the concrete {@code EventFieldInfo} represented by the builder.
     *
     * @return {@code EventFieldInfo} represented by the builder
     */
    protected abstract TEventFieldInfo buildInternal();
}
