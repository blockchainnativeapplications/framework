package org.blockchainnative.fabric;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.convert.TypedObjectHolder;
import org.blockchainnative.exceptions.TypeConvertException;
import org.blockchainnative.fabric.metadata.FabricEventFieldInfo;
import org.blockchainnative.fabric.metadata.FabricEventInfo;
import org.blockchainnative.fabric.metadata.FabricMethodInfo;
import org.blockchainnative.fabric.metadata.FabricParameterInfo;
import org.blockchainnative.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Matthias Veit
 * @since 1.0
 */
public class FabricArgumentConverterImpl implements FabricArgumentConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricArgumentConverterImpl.class);

    private final TypeConverters typeConverters;

    /**
     * Creates a new {@code FabricArgumentConverterImpl}
     *
     * @param typeConverters type converters to be used to convert the arguments
     */
    public FabricArgumentConverterImpl(TypeConverters typeConverters) {
        this.typeConverters = typeConverters;
    }

    @Override
    public Object createEventObject(FabricEventInfo eventInfo, Object eventData) {

        if (eventInfo.getEventFieldInfos().isEmpty()) {
            var message = String.format("Cannot create event object for event '%s', no EventFieldInfo registered", eventInfo.getEventName());
            LOGGER.error(message);
            throw new TypeConvertException(message);
        }
        if (eventInfo.getEventFieldInfos().size() > 1) {
            LOGGER.warn("EventInfo for event '{}' contained more than one EventFieldInfo object, however, Hyperledger Fabric only supports a single event value.", eventInfo.getEventName());
        }

        var eventFieldInfo = eventInfo.getEventFieldInfos().get(0);
        var value = convertEventField(eventFieldInfo, eventData);
        try {
            var type = eventInfo.getEventType();
            var instance = type.getDeclaredConstructor().newInstance();

            var field = eventFieldInfo.getField();

            if (PropertyUtils.isWriteable(instance, field.getName())) {
                PropertyUtils.setProperty(instance, field.getName(), value);
            } else {
                FieldUtils.writeField(field, instance, value, true);
            }

            return instance;

        } catch (NoSuchMethodException e) {
            var message = String.format("Failed to instantiate event type '%s', no default constructor found", eventInfo.getEventType().getName());
            LOGGER.error(message, e);
            throw new TypeConvertException(message, e);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            var message = String.format("Failed to set properties of event type '%s'", eventInfo.getEventType().getName());
            LOGGER.error(message, e);
            throw new TypeConvertException(message, e);
        }
    }

    @Override
    public List<String> convertArguments(List<FabricParameterInfo> parameterInfos, Object[] arguments) {
        var convertedArgs = new ArrayList<String>(arguments.length);

        if (arguments.length != parameterInfos.size())
            throw new TypeConvertException("Number of given arguments differ from the ones defined in ContractInfo");

        for (var i = 0; i < arguments.length; i++) {
            var parameterInfo = parameterInfos.get(i);
            var argument = arguments[i];

            if (argument == null) continue;

            // Casting to most "generic" type Object must not fail
            // noinspection unchecked
            @SuppressWarnings("unchecked")
            var fromType = (Class<Object>) parameterInfo.getParameter().getType();
            var passAsType = parameterInfo.getPassParameterAsType(); // passAsType needs to be String, checked below
            var typeConverterClass = parameterInfo.getTypeConverterClass();

            if (passAsType.isPresent() && typeConverterClass.isPresent()) {
                LOGGER.warn("Both, passAsType and useTypeConverterClass are specified for field '{}'. passAsType is going to be ignored", parameterInfo.getParameterIndex());
            }

            Object convertedArgument;
            var typedArgument = new TypedObjectHolder(parameterInfo.getParameter().getParameterizedType(), argument);
            // use type converter if present
            if (typeConverterClass.isPresent()) {
                LOGGER.debug("Type converter '{}' is specified for converting field {}", typeConverterClass.get().getName(), parameterInfo.getParameterIndex());
                convertedArgument = this.typeConverters.convertObjectUsingTypeConverterClass(typedArgument, typeConverterClass.get());
            } else if (passAsType.isPresent()) {
                if (passAsType.get() != String.class) {
                    var message = String.format("Paramter %s of method '%s' should be passed as type '%s' to the underlying API, however, currently on String is allowed for Hyperledger Fabric", parameterInfo.getParameterIndex(), parameterInfo.getParameter().getDeclaringExecutable().getName());
                    LOGGER.error(message);
                    throw new TypeConvertException(message);
                }
                LOGGER.debug("passAsType '{}' is specified for converting field {}", passAsType.get().getName(), parameterInfo.getParameterIndex());
                var intermediateArgument = this.typeConverters.convertObjectUsingMatchingTypeConverter(typedArgument, passAsType.get(), false);
                if (intermediateArgument == null) {
                    convertedArgument = argument;
                } else {
                    convertedArgument = intermediateArgument;
                }
            } else {
                convertedArgument = argument;
            }

            if (convertedArgument instanceof String) {
                // no conversion necessary
                convertedArgs.add((String) convertedArgument);
            } else {
                // if the field is not yet of the required type, try conversion
                Optional<Function<Object, String>> converter = this.typeConverters.getConversionFunction(fromType, String.class);
                if (!converter.isPresent()) {
                    throw new TypeConvertException(String.format("No type converter registered for types '%s' and '%s'!", fromType.getName(), String.class.getName()));
                }
                convertedArgs.add(converter.get().apply(convertedArgument));
            }
        }

        return convertedArgs;
    }

    @Override
    public Object convertMethodResult(FabricMethodInfo methodInfo, Object output) {
        if (methodInfo.isVoidReturnType()) {
            return Void.TYPE;
        }
        return convertResult(ReflectionUtil.getActualReturnType(methodInfo.getMethod()), methodInfo.getResultTypeConverterClass(), output);
    }

    private Object convertEventField(FabricEventFieldInfo eventFieldInfo, Object result) {
        return convertResult(eventFieldInfo.getField().getGenericType(), eventFieldInfo.getTypeConverterClass(), result);
    }

    private Object convertResult(Type declaredReturnType, Optional<Class<? extends TypeConverter<?, ?>>> resultTypeConverterClass, Object output) {
        if (output == null) {
            return null;
        }

        // if types match, simply return it
        if (TypeUtils.isInstance(output, declaredReturnType)) {
            return output;
        }

        Object convertedResult;
        var typedResult = new TypedObjectHolder(output.getClass(), output);
        // use result type converter if  present
        // otherwise, try find a matching converter
        if (resultTypeConverterClass.isPresent()) {
            convertedResult = this.typeConverters.convertObjectUsingTypeConverterClass(typedResult, resultTypeConverterClass.get());
        } else {
            convertedResult = this.typeConverters.convertObjectUsingMatchingTypeConverter(typedResult, declaredReturnType);
        }

        if (!TypeUtils.isInstance(convertedResult, declaredReturnType)) {
            throw new TypeConvertException(String.format("Converted argument does not match the declared result type '%s', verify that the specified type converter returns the correct type.", declaredReturnType.getTypeName()));
        }

        return convertedResult;
    }
}
