package org.blockchainnative.quorum;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.convert.TypedObjectHolder;
import org.blockchainnative.ethereum.util.AbiUtil;
import org.blockchainnative.exceptions.ContractCallException;
import org.blockchainnative.exceptions.TypeConvertException;
import org.blockchainnative.quorum.metadata.QuorumEventFieldInfo;
import org.blockchainnative.quorum.metadata.QuorumEventInfo;
import org.blockchainnative.quorum.metadata.QuorumMethodInfo;
import org.blockchainnative.quorum.metadata.QuorumParameterInfo;
import org.blockchainnative.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.AbiTypes;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @since 1.1
 * @author Matthias Veit
 */
public class QuorumArgumentConverterImpl implements QuorumArgumentConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuorumArgumentConverterImpl.class);

    private static final TypeVariable<?> collectionTypeVariable = Collection.class.getTypeParameters()[0];
    private static final TypeVariable<?> listTypeVariable = List.class.getTypeParameters()[0];

    // Events
    private static final TypeVariable<?> setTypeVariable = Set.class.getTypeParameters()[0];

    // Arguments
    private final TypeConverters typeConverters;

    /**
     * Creates a new {@code QuorumArgumentConverterImpl}
     *
     * @param typeConverters type converters to be used to convert the arguments
     */
    public QuorumArgumentConverterImpl(TypeConverters typeConverters) {
        this.typeConverters = typeConverters;
    }

    @Override
    public Object createEventObject(QuorumEventInfo eventInfo, EventValues eventValues) {

        try {
            var type = eventInfo.getEventType();
            var instance = type.getDeclaredConstructor().newInstance();

            var indexedParameterCount = 0;
            var nonIndexedParameterCount = 0;
            for (var eventFieldInfo : eventInfo.getEventFieldInfos()) {
                org.web3j.abi.datatypes.Type eventValue = null;
                if (eventFieldInfo.getSolidityType().isIndexed()) {
                    if (indexedParameterCount > (eventValues.getIndexedValues().size() - 1)) {
                        LOGGER.warn(String.format("Event did not contain value for field '%s'", eventFieldInfo.getField().getName()));
                    } else {
                        eventValue = eventValues.getIndexedValues().get(indexedParameterCount);
                    }
                    indexedParameterCount++;
                } else {
                    if (nonIndexedParameterCount > (eventValues.getNonIndexedValues().size() -1)) {
                        LOGGER.warn(String.format("Event did not contain value for field '%s'", eventFieldInfo.getField().getName()));
                    } else {
                        eventValue = eventValues.getNonIndexedValues().get(nonIndexedParameterCount);
                    }
                    nonIndexedParameterCount++;
                }
                if (eventValue != null) {
                    Object value = this.convertEventField(eventFieldInfo, eventValue);

                    var field = eventFieldInfo.getField();

                    if (PropertyUtils.isWriteable(instance, field.getName())) {
                        PropertyUtils.setProperty(instance, field.getName(), value);
                    } else {
                        FieldUtils.writeField(field, instance, value, true);
                    }
                }
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
    public List<Type> convertArguments(List<QuorumParameterInfo> parameterInfos, Object[] arguments) {
        if (parameterInfos == null) throw new IllegalArgumentException("parameterInfos must not be null");
        if (arguments == null) throw new IllegalArgumentException("arguments must not be null");
        if (parameterInfos.size() != arguments.length)
            throw new IllegalArgumentException(String.format("Each argument needs a corresponding eventFieldInfo, given number of arguments: '%s', given number of parameterInfos: '%s'", arguments.length, parameterInfos.size()));

        var convertedArgs = new ArrayList<Type>(arguments.length);

        for (var i = 0; i < arguments.length; i++) {
            var parameterInfo = parameterInfos.get(i);
            var argument = arguments[i];

            if (argument != null) {
                convertedArgs.add(convertArgument(parameterInfo, argument));
            } else {
                convertedArgs.add(null);
            }
        }
        return convertedArgs;
    }

    @Override
    public Object convertMethodResult(QuorumMethodInfo methodInfo, List<org.web3j.abi.datatypes.Type> results) {
        if (methodInfo.isVoidReturnType()) {
            return Void.TYPE;
        }

        if(!methodInfo.isVoidReturnType() && (results == null || results.isEmpty())){
            throw new ContractCallException(String.format("Contract method '%s' did not yield any results but the its wrapper return type is declared as '%s'", methodInfo.getContractMethodName(), methodInfo.getMethod().getGenericReturnType().getTypeName()));
        }

        return convertResult(ReflectionUtil.getActualReturnType(methodInfo.getMethod()), methodInfo.getResultTypeConverterClass(), results);
    }


    private Object convertEventField(QuorumEventFieldInfo eventFieldInfo, org.web3j.abi.datatypes.Type result) {
        return convertResult(eventFieldInfo.getField().getGenericType(), eventFieldInfo.getTypeConverterClass(), new ArrayList<>() {{
            add(result);
        }});
    }

    @Override
    public List<TypeReference<?>> getInputParameterTypesReferences(AbiDefinition methodAbi) {
        return getTypeReferences(methodAbi.getInputs());
    }

    @Override
    public List<TypeReference<?>> getOutputParameterTypeReferences(AbiDefinition methodAbi) {
        return getTypeReferences(methodAbi.getOutputs());
    }

    private static TypeReference<?> getTypeReferenceFromString(AbiDefinition.NamedType type) {
        var typeParameter = getWeb3jParameterizedTypeFromString(AbiUtil.stripLocationFromType(type.getType()));
        try {
            return new ByteBuddy()
                    .subclass(net.bytebuddy.description.type.TypeDescription.Generic.Builder.parameterizedType(TypeReference.class, typeParameter).build(), ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                    .make()
                    .load(QuorumContractWrapper.class.getClassLoader())
                    .getLoaded()
                    .asSubclass(TypeReference.class)
                    .getDeclaredConstructor(Boolean.TYPE).newInstance(type.isIndexed());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new TypeConvertException(String.format("Failed to create return type reference for type '%s'.", type.getName()), e);
        }
    }

    // We cannot directly use Arrays.asList((Object[]) argument)
    // because if argument is a primitive array, the cast to Object[] will fail
    private static Iterable<Object> arrayAsIterable(Object argument) {
        if (argument.getClass().getComponentType().isPrimitive()) {
            int length = Array.getLength(argument);
            Object[] tmp = new Object[length];
            for (var i = 0; i < length; i++) {
                tmp[i] = Array.get(argument, i);
            }
            argument = tmp;
        }
        return Arrays.asList((Object[]) argument);
    }

    @SuppressWarnings("unchecked")
    private static org.web3j.abi.datatypes.Array constructArray(Optional<Integer> dimension, List<Object> items) {
        if (dimension.isPresent()) {
            return constructStaticArray(dimension.get(), items);
        } else {
            return new org.web3j.abi.datatypes.DynamicArray(items);
        }
    }

    @SuppressWarnings("unchecked")
    private static org.web3j.abi.datatypes.StaticArray constructStaticArray(int dimension, List<Object> items) {
        try {
            var arrayType = (Class<? extends org.web3j.abi.datatypes.StaticArray>) Class.forName("org.web3j.abi.datatypes.generated.StaticArray" + dimension);
            return arrayType.getConstructor(List.class).newInstance(items);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            return new org.web3j.abi.datatypes.StaticArray(dimension, items);
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends org.web3j.abi.datatypes.StaticArray> getStaticArrayType(int dimension) {
        try {
            return (Class<? extends org.web3j.abi.datatypes.StaticArray>) Class.forName("org.web3j.abi.datatypes.generated.StaticArray" + dimension);
        } catch (ClassNotFoundException e) {
            return org.web3j.abi.datatypes.StaticArray.class;
        }
    }

    private static Object checkResult(Object result, java.lang.reflect.Type declaredReturnType) {
        if (TypeUtils.isInstance(result, declaredReturnType)) {
            return result;
        } else {
            throw new TypeConvertException(String.format("Failed to convert result to target type '%s'", declaredReturnType.getTypeName()));
        }
    }

    private static List<TypeReference<?>> getTypeReferences(List<AbiDefinition.NamedType> types) {
        return types.stream()
                .map(type -> getTypeReferenceFromString(type))
                .collect(Collectors.toList());
    }

    private static java.lang.reflect.Type getNativeParameterizedType(Type type) {
        return getNativeParameterizedTypeFromString(AbiUtil.stripLocationFromType(type.getTypeAsString()));
    }

    private static java.lang.reflect.Type getNativeParameterizedTypeFromString(String typeName) {
        var arrayPattern = Pattern.compile("(.*)\\[(\\d*)]$");
        var matcher = arrayPattern.matcher(typeName);

        if (matcher.matches()) {
            var dimensionGroup = matcher.group(2);
            var remainder = typeName.substring(0, typeName.length() - 2 - dimensionGroup.length());

            var inner = getNativeParameterizedTypeFromString(remainder);
            // Both static and dynamic arrays are converted to Lists
            var outer = List.class;

            return TypeUtils.parameterize(outer, inner);
        }

        return getNativeTypeForSoliditySimpleType(typeName);
    }

    private static java.lang.reflect.Type getWeb3jParameterizedTypeFromString(String typeName) {
        var arrayPattern = Pattern.compile("(.*)\\[(\\d*)]$");
        var matcher = arrayPattern.matcher(typeName);

        if (matcher.matches()) {
            var dimensionGroup = matcher.group(2);
            Optional<Integer> dimension;
            if (!"".equals(dimensionGroup)) {
                dimension = Optional.of(Integer.parseInt(dimensionGroup));
            } else {
                dimension = Optional.empty();
            }
            var remainder = typeName.substring(0, typeName.length() - 2 - dimensionGroup.length());

            var inner = getWeb3jParameterizedTypeFromString(remainder);

            Class<?> outer;
            if (dimension.isPresent()) {
                outer = getStaticArrayType(dimension.get());
            } else {
                outer = org.web3j.abi.datatypes.DynamicArray.class;
            }

            return TypeUtils.parameterize(outer, inner);
        }

        return org.web3j.abi.datatypes.generated.AbiTypes.getType(typeName);
    }

    private static boolean isSupportedCollectionType(java.lang.reflect.Type declaredType) {
        return ((declaredType instanceof ParameterizedType)
                && (((ParameterizedType) declaredType).getRawType().equals(Collection.class)
                || ((ParameterizedType) declaredType).getRawType().equals(List.class)
                || ((ParameterizedType) declaredType).getRawType().equals(Set.class)));
    }

    private static java.lang.reflect.Type unwrapSupportCollectionType(java.lang.reflect.Type declaredReturnType) {
        if (TypeUtils.isArrayType(declaredReturnType)) {
            return TypeUtils.getArrayComponentType(declaredReturnType);
        } else if (declaredReturnType instanceof ParameterizedType) {
            var declaredParameterizedType = (ParameterizedType) declaredReturnType;

            if (declaredParameterizedType.getRawType().equals(Collection.class)) {
                var typeArguments = TypeUtils.getTypeArguments(declaredParameterizedType, Collection.class);
                return typeArguments.get(collectionTypeVariable);
            } else if (declaredParameterizedType.getRawType().equals(List.class)) {
                var typeArguments = TypeUtils.getTypeArguments(declaredParameterizedType, List.class);
                return typeArguments.get(listTypeVariable);
            } else if (declaredParameterizedType.getRawType().equals(Set.class)) {
                var typeArguments = TypeUtils.getTypeArguments(declaredParameterizedType, Set.class);
                return typeArguments.get(setTypeVariable);
            } else {
                throw new IllegalArgumentException(String.format("Unsupported collection type '%s'", declaredReturnType.getTypeName()));
            }
        } else {
            throw new IllegalArgumentException(String.format("Unsupported collection type '%s'", declaredReturnType.getTypeName()));
        }
    }

    private static Object convertToSupportedCollectionType(List<Object> objects, java.lang.reflect.Type declaredReturnType) {
        if (TypeUtils.isArrayType(declaredReturnType)) {
            return objects.toArray();
        } else if (declaredReturnType instanceof ParameterizedType) {
            var declaredParameterizedType = (ParameterizedType) declaredReturnType;

            if (declaredParameterizedType.getRawType().equals(Collection.class)
                    || declaredParameterizedType.getRawType().equals(List.class)) {
                return objects;
            } else if (declaredParameterizedType.getRawType().equals(Set.class)) {
                return new HashSet<>(objects);
            } else {
                throw new IllegalArgumentException(String.format("Unsupported collection type '%s'", declaredReturnType.getTypeName()));
            }
        } else {
            throw new IllegalArgumentException(String.format("Unsupported collection type '%s'", declaredReturnType.getTypeName()));
        }
    }

    private static Class<?> getNativeTypeForSoliditySimpleType(String typeName) {
        var web3jTypeTypeArgument = Type.class.getTypeParameters()[0];

        var web3jType = AbiTypes.getType(typeName);
        var actualTypeArguments = TypeUtils.getTypeArguments(web3jType, Type.class);

        if (actualTypeArguments.size() != 1) {
            throw new RuntimeException(String.format("Unexpected Number of type arguments of type '%s'", web3jType));
        }

        var typeArgument = actualTypeArguments.get(web3jTypeTypeArgument);

        if (typeArgument instanceof Class<?>) {
            return (Class<?>) typeArgument;
        } else if (TypeUtils.isArrayType(typeArgument)) {
            var x = TypeUtils.getRawType(TypeUtils.getArrayComponentType(typeArgument), null);
            return Array.newInstance(x, 0).getClass();
        }

        throw new RuntimeException(String.format("Unexpected type argument '%s'", typeArgument.getTypeName()));

    }

    private static List<Object> extractNativeTypes(List<org.web3j.abi.datatypes.Type> typeList) {
        return typeList.stream()
                .map(type -> extractNativeType(type))
                .collect(Collectors.toList());
    }

    private static Object extractNativeType(Object object) {
        if (object instanceof Type) {
            return extractNativeType(((Type) object).getValue());
        }
        if (object instanceof List) {
            return ((List<?>) object).stream().map(QuorumArgumentConverterImpl::extractNativeType).collect(Collectors.toList());
        }
        return object;
    }

    private org.web3j.abi.datatypes.Type convertArgument(QuorumParameterInfo parameterInfo, Object argument) {
        if (parameterInfo == null) throw new IllegalArgumentException("eventFieldInfo must not be null");
        if (argument == null) throw new IllegalArgumentException("argument must not be null");

        var passAsType = parameterInfo.getPassParameterAsType();
        var typeConverterClass = parameterInfo.getTypeConverterClass();

        if (passAsType.isPresent() && typeConverterClass.isPresent()) {
            LOGGER.warn("Both, passAsType and useTypeConverterClass are specified for field '{}'. passAsType is going to be ignored", parameterInfo.getParameterIndex());
        }

        Object convertedArgument;
        var typedArgument = new TypedObjectHolder(parameterInfo.getParameter().getParameterizedType(), argument);
        // use type converter if present
        if (typeConverterClass.isPresent()) {
            convertedArgument = this.typeConverters.convertObjectUsingTypeConverterClass(typedArgument, typeConverterClass.get());
        } else if (passAsType.isPresent()) {
            convertedArgument = this.typeConverters.convertObjectUsingMatchingTypeConverter(typedArgument, passAsType.get());
        } else {
            convertedArgument = argument;
        }

        if (convertedArgument instanceof org.web3j.abi.datatypes.Type<?>) {
            // no conversion should be necessary
            // but check it to be sure
            if (parameterInfo.getSolidityType().equals(((org.web3j.abi.datatypes.Type<?>) convertedArgument).getTypeAsString())) {
                throw new TypeConvertException(String.format("Defined and given type of argument '%s' differ. expected: '%s', actual: '%s'.", argument, parameterInfo.getSolidityType(), ((org.web3j.abi.datatypes.Type<?>) argument).getTypeAsString()));
            }
            return (org.web3j.abi.datatypes.Type) convertedArgument;
        } else {
            // if the field is not yet of the required type, try conversion
            return convertToSolidityType(convertedArgument, parameterInfo.getSolidityType());
        }
    }

    private org.web3j.abi.datatypes.Type convertToSolidityType(Object argument, String typeName) {
        // solidity arrays are defined in a weird way, string[3][2] corresponds to a java array of String[2][3]
        var arrayPattern = Pattern.compile("(.*)\\[(\\d*)]$");
        var matcher = arrayPattern.matcher(typeName);

        if (matcher.matches()) {
            var dimensionGroup = matcher.group(2);
            Optional<Integer> dimension;
            if (!"".equals(dimensionGroup)) {
                dimension = Optional.of(Integer.parseInt(dimensionGroup));
            } else {
                dimension = Optional.empty();
            }
            var remainder = typeName.substring(0, typeName.length() - 2 - dimensionGroup.length());

            if (argument.getClass().isArray()) {
                argument = arrayAsIterable(argument);
            }

            if (!(argument instanceof Iterable)) {
                throw new TypeConvertException(String.format("Cannot convert argument '%s' to type '%s', argument is not Iterable.", argument, typeName));
            }

            List<Object> inner = StreamSupport.stream(((Iterable<?>) argument).spliterator(), false)
                    .map(x -> convertToSolidityType(x, remainder))
                    .collect(Collectors.toList());

            return constructArray(dimension, inner);
        }

        try {
            return convertSimpleType(argument, typeName);
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new TypeConvertException(String.format("Failed to convert field to type '%s'", typeName), e);
        }
    }

    private Type<?> convertSimpleType(Object argument, String typeName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        @SuppressWarnings("unchecked")
        var type = (Class<? extends Type>) AbiTypes.getType(typeName);
        if (Int.class.isAssignableFrom(type) || Uint.class.isAssignableFrom(type)) {
            if (!(argument instanceof Number)) {
                argument = this.typeConverters.convertObjectUsingMatchingTypeConverter(new TypedObjectHolder(argument.getClass(), argument), BigInteger.class);
            }
            return type.getConstructor(Long.TYPE).newInstance(((Number) argument).longValue());
        } else if (Bool.class.isAssignableFrom(type)) {
            if (!(argument instanceof Boolean)) {
                argument = this.typeConverters.convertObjectUsingMatchingTypeConverter(new TypedObjectHolder(argument.getClass(), argument), Boolean.class);
            }
            return type.getConstructor(Boolean.class).newInstance((Boolean) argument);
        } else if (Address.class.isAssignableFrom(type) || Utf8String.class.isAssignableFrom(type)) {
            if (!(argument instanceof String)) {
                argument = this.typeConverters.convertObjectUsingMatchingTypeConverter(new TypedObjectHolder(argument.getClass(), argument), String.class);
            }
            return type.getConstructor(String.class).newInstance((String) argument);
        } else if (BytesType.class.isAssignableFrom(type)) {
            if (!(argument instanceof byte[])) {
                argument = this.typeConverters.convertObjectUsingMatchingTypeConverter(new TypedObjectHolder(argument.getClass(), argument), byte[].class);
            }
            // it is intended to call the constructor with a single argument
            // noinspection PrimitiveArrayArgumentToVarargsMethod
            return type.getConstructor(byte[].class).newInstance((byte[]) argument);
        } else {
            throw new TypeConvertException(String.format("Cannot convert argument '%s' to unexpected type '%s'.", argument, type));
        }
    }

    private Object convertResult(java.lang.reflect.Type declaredReturnType, Optional<Class<? extends TypeConverter<?, ?>>> resultTypeConverterClass, List<org.web3j.abi.datatypes.Type> results) {
        if (results == null) {
            return null;
        }

        // extract java native types from web3j type wrappers
        var nativeResults = extractNativeTypes(results);

        // in the initial step, try to use the type converter to convert the list of results (List<Object>) to the declared return type
        // We don't want an exception to be raised when the type converter does not accept such List
        var objectHolder = new TypedObjectHolder(
                TypeUtils.parameterize(List.class, Object.class), nativeResults);

        Object result;
        if (resultTypeConverterClass.isPresent()) {
            result = this.typeConverters.convertObjectUsingTypeConverterClass(objectHolder, resultTypeConverterClass.get(), false);
        } else {
            // TODO think about whether it is useful to find any converter from List<Object> -> return type
            result = this.typeConverters.convertObjectUsingMatchingTypeConverter(objectHolder, declaredReturnType, false);
        }

        try {
            if (result != null) {
                // if we already got a result from type conversion, return it
                return checkResult(result, declaredReturnType);

            } else if (results.size() == 1) {
                // Otherwise, if its a single result, scrap outer List and try again
                var singleNativeResult = nativeResults.get(0);
                var outputType = getNativeParameterizedType(results.get(0));
                objectHolder = new TypedObjectHolder(outputType, singleNativeResult);

                return checkResult(convertResultInternal(declaredReturnType, resultTypeConverterClass, objectHolder), declaredReturnType);

            } else {
                // in this case we have multiple results from the contract and no type converter specified or the conversion failed
                // if the declared return type is as supported collection ob Object, we can convert and return it,
                // otherwise it will fail in checkResult

                return checkResult(convertResultInternal(declaredReturnType, Optional.empty(), objectHolder), declaredReturnType);
            }
        } catch (IllegalStateException e) {
            throw new TypeConvertException(String.format("Failed to convert result to target type '%s'", declaredReturnType.getTypeName()), e);
        }
    }

    private Object convertResultInternal(java.lang.reflect.Type declaredReturnType, Optional<Class<? extends TypeConverter<?, ?>>> resultTypeConverterClass, TypedObjectHolder objectHolder) {
        // if the single result is already of an acceptable type, return it
        if (TypeUtils.isAssignable(objectHolder.getType(), declaredReturnType)) {
            return objectHolder.getObject();
        }

        // Since we don't have the correct type, try to find a type converter
        Object result;
        if (resultTypeConverterClass.isPresent()) {
            // 1.   Try to use type declared converter class
            result = this.typeConverters.convertObjectUsingTypeConverterClass(objectHolder, resultTypeConverterClass.get());
        } else {
            // 2.   Try to find a matching type converter
            result = this.typeConverters.convertObjectUsingMatchingTypeConverter(objectHolder, declaredReturnType, false);
        }


        // 3.   if the conversion with type converters was unsuccessful
        //      but the declared return type is a supported collection
        //      try decomposing the result list repeat from step 2
        if (result == null
                && isSupportedCollectionType(declaredReturnType)
                && TypeUtils.isInstance(objectHolder.getObject(), List.class)) {

            var innerDeclaredType = unwrapSupportCollectionType(declaredReturnType);
            var innerActualType = unwrapSupportCollectionType(objectHolder.getType()); // strip the List
            var intermediateResultList = ((List<?>) objectHolder.getObject()).stream()
                    .map(o -> convertResultInternal(innerDeclaredType, Optional.empty(), new TypedObjectHolder(innerActualType, o)))
                    .collect(Collectors.toList());

            result = convertToSupportedCollectionType(intermediateResultList, declaredReturnType);
        }

        // 4.   Try to match the method return type in case we only have a single result
        //      and the method specifies a primitive number as return type
        //      This way we can specify int as return type whereas web3j returns BigIntegers
        if (result == null
                && declaredReturnType instanceof Class<?>) {

            var wrapperType = ClassUtils.primitiveToWrapper((Class<?>) declaredReturnType);
            if (objectHolder.getObject() instanceof Number
                    && ((Class<?>) declaredReturnType).isPrimitive()
                    && Number.class.isAssignableFrom(wrapperType)) {
                result = numberToPrimitive((Number) objectHolder.getObject(), wrapperType);
            }
        }

        if (result != null) {
            return result;
        } else {
            throw new IllegalStateException(String.format("Implicit conversion failed, unable to convert '%s' to '%s'", objectHolder.getType().getTypeName(), declaredReturnType.getTypeName()));
        }
    }

    private Object numberToPrimitive(Number number, Class<?> type) {
        if (Byte.class.equals(type)) {
            return number.byteValue();
        } else if (Short.class.equals(type)) {
            return number.shortValue();
        } else if (Integer.class.equals(type)) {
            return number.intValue();
        } else if (java.lang.Long.class.equals(type)) {
            return number.longValue();
        } else if (Float.class.equals(type)) {
            return number.floatValue();
        } else if (Double.class.equals(type)) {
            return number.doubleValue();
        } else {
            throw new IllegalArgumentException(String.format("Cannot convert type '%s' to primitive number type!", type.getName()));
        }
    }
}
