package org.blockchainnative.convert;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.blockchainnative.exceptions.TypeConvertException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a colleciton of {@link TypeConverter} with additional lookup logic. <br>
 * <br>
 * Due to Java's implementation of generic types, each {@code TypeConverter} in the collection needs to bind the type parameters {@code TTo} and {@code TFrom} to a concrete type in order for the lookup functions to work properly.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class TypeConverters extends HashSet<TypeConverter<?, ?>> {

    private static final TypeVariable<?> fromTypeVariable;
    private static final TypeVariable<?> toTypeVariable;

    static {
        var typeConverterTypeArguments = TypeConverter.class.getTypeParameters();

        fromTypeVariable = typeConverterTypeArguments[0]; // TFrom is the first parameter of TypeConverter<TFrom, TTo>
        toTypeVariable = typeConverterTypeArguments[1]; // TTo is the second parameter of TypeConverter<TFrom, TTo>
    }

    /**
     * Constructs a new empty set of {@code TypeConverters}
     */
    public TypeConverters() {
    }

    /**
     * Constructs a new set of {@code TypeConverters} containing the elements in the specified collection
     *
     * @param c the collection whose elements are to be placed into this set
     * @throws NullPointerException if the specified collection is null
     */
    public TypeConverters(Collection<? extends TypeConverter<?, ?>> c) {
        super(c);
    }

    /**
     * Constructs a new set of {@code TypeConverters} containing the elements in the specified array
     *
     * @param typeConverters the array whose elements are to be placed into this set
     * @throws NullPointerException if the specified array is null
     */
    public TypeConverters(TypeConverter<?, ?>... typeConverters) {
        this(Arrays.asList(typeConverters));
    }

    /**
     * Tries to find a type converter whose type parameters match the given types.
     *
     * @param fromType type to match with a type converter's type parameter {@code TFrom}
     * @param toType   type to match with a type converter's type parameter {@code TTo}
     * @param <TFrom>  type parameter representing {@code fromType}
     * @param <TTo>    type parameter representing {@code toType}
     * @return type Optional containing a converter that is able to convert objects from type {@code fromType} to type {@code toType} or empty Optional
     */
    @SuppressWarnings("unchecked")
    public <TFrom, TTo> Optional<TypeConverter<TFrom, TTo>> getTypeConverter(Type fromType, Type toType) {
        return this.stream()
                .filter(typeConverter ->
                        TypeUtils.isAssignable(fromType, getFromType(typeConverter))
                                && TypeUtils.isAssignable(toType, getToType(typeConverter)))
                .map(typeConverter -> (TypeConverter<TFrom, TTo>) typeConverter)
                .findFirst();
    }

    /**
     * Returns an instance of the given {@code TypeConverter} class. <br>
     * <br>
     * If no instance of the given type is registered, a new instace will be created using class' default constructor.
     *
     * @param converterType class of the converter to be found.
     * @return an instance of the given {@code TypeConverter} class
     * @throws TypeConvertException If no instance of the given type is registered, and the given class does not define a parameterless constructor.
     */
    public TypeConverter<?, ?> getTypeConverter(Class<? extends TypeConverter<?, ?>> converterType) {
        if (converterType == null) throw new IllegalArgumentException("converterType must not be null!");

        try {
            return this.stream()
                    .filter(typeConverter -> typeConverter.getClass().equals(converterType))
                    .findFirst()
                    .orElse(converterType.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new TypeConvertException(String.format("No instance of type converter '%s' has been found and creation via default constructor failed. Register the an instance of this type converter or add a default constructor to the class.", converterType.getName()), e);
        }
    }

    /**
     * Returns a {@code Function} targeting a {@code TypeConverter}'s {@code to()} or {@code from()} function, depending on the given types.
     *
     * @param fromType type to match with a type converter's type parameter {@code TFrom}
     * @param toType   type to match with a type converter's type parameter {@code TTo}
     * @param <TFrom>  type parameter representing {@code fromType}
     * @param <TTo>    type parameter representing {@code toType}
     * @return {@code Function} to convert from {@code fromType} to {@code toType}.
     */
    public <TFrom, TTo> Optional<Function<TFrom, TTo>> getConversionFunction(Type fromType, Type toType) {
        Optional<TypeConverter<TFrom, TTo>> converter = this.getTypeConverter(fromType, toType);
        if (converter.isPresent()) {
            return Optional.of(converter.get()::to);
        }
        Optional<TypeConverter<TTo, TFrom>> reversedConverter = this.getTypeConverter(toType, fromType);
        if (reversedConverter.isPresent()) {
            return Optional.of(reversedConverter.get()::from);
        }
        return Optional.empty();
    }

    /**
     * Converts the object contained in the given {@code TypedObjectHolder} using the given type converter class.
     *
     * @param objectHolder       object to be converted together with its type information
     * @param typeConverterClass type converter class to be used to convert the object
     * @return the converted object
     * @throws TypeConvertException in case an error occurs during type conversion or if the given type converter is not able to convert the object contained in {@code objectHolder}
     */
    public Object convertObjectUsingTypeConverterClass(TypedObjectHolder objectHolder, Class<? extends TypeConverter<?, ?>> typeConverterClass) {
        return convertObjectUsingTypeConverterClass(objectHolder, typeConverterClass, true);
    }

    /**
     * Converts the object contained in the given {@code TypedObjectHolder} using the given type converter class.
     *
     * @param objectHolder            object to be converted together with its type information
     * @param typeConverterClass      type converter class to be used to convert the object
     * @param throwOnInvalidConverter indicates whether or not the method shall throw an exception if the given type converter is not able to convert the object contained in {@code objectHolder}
     * @return the converted object or {@code null} if the given type converter is not able to convert the object contained in {@code objectHolder} and {@code throwOnInvalidConverter} is set to {@code false}
     * @throws TypeConvertException in case an error occurs during type conversion or if the given type converter is not able to convert the object contained in {@code objectHolder} and {@code throwOnInvalidConverter} is set to {@code false}
     */
    @SuppressWarnings("unchecked")
    public Object convertObjectUsingTypeConverterClass(TypedObjectHolder objectHolder, Class<? extends TypeConverter<?, ?>> typeConverterClass, boolean throwOnInvalidConverter) {
        var converter = (TypeConverter<Object, Object>) this.getTypeConverter(typeConverterClass);
        if (TypeUtils.isAssignable(objectHolder.getType(), getFromType(converter))) {

            try {
                return converter.from(objectHolder);
            } catch (Exception e) {
                throw new TypeConvertException("Failed to convert value", e);
            }
        } else if (TypeUtils.isAssignable(objectHolder.getType(), getToType(converter))) {
            try {
                return converter.to(objectHolder);
            } catch (Exception e) {
                throw new TypeConvertException("Failed to convert value", e);
            }
        } else {
            if (throwOnInvalidConverter) {
                throw new TypeConvertException(String.format("Registered type converter '%s' is not suitable for converting field of type '%s'!", typeConverterClass.getName(), objectHolder.getClass().getName()));
            } else {
                return null;
            }
        }
    }

    /**
     * Converts the object contained in the given {@code TypedObjectHolder} to the given target type using any matching type converter.
     *
     * @param objectHolder object to be converted together with its type information
     * @param targetType   type to which the given object shall be converted
     * @return the converted object
     * @throws TypeConvertException in case an error occurs during type conversion or if no type converter is found that is able to convert the object contained in {@code objectHolder}.
     */
    public Object convertObjectUsingMatchingTypeConverter(TypedObjectHolder objectHolder, Type targetType) {
        return convertObjectUsingMatchingTypeConverter(objectHolder, targetType, true);
    }


    /**
     * Converts the object contained in the given {@code TypedObjectHolder} to the given target type using any matching type converter.
     *
     * @param objectHolder            object to be converted together with its type information
     * @param targetType              type to which the given object shall be converted
     * @param throwOnMissingConverter indicates whether or not the method shall throw an exception if no type converter is found that is able to convert the object contained in {@code objectHolder}
     * @return the converted object or {@code null} if no type converter is found that is able to convert the object contained in {@code objectHolder} and {@code throwOnMissingConverter} is set to {@code false}
     * @throws TypeConvertException in case an error occurs during type conversion or if no type converter is found that is able to convert the object contained in {@code objectHolder} and {@code throwOnMissingConverter} is set to {@code false}.
     */
    @SuppressWarnings("unchecked")
    public Object convertObjectUsingMatchingTypeConverter(TypedObjectHolder objectHolder, Type targetType, boolean throwOnMissingConverter) {
        var objectType = objectHolder.getType();

        var converter = this.getConversionFunction(objectType, targetType);
        if (!converter.isPresent()) {
            if (throwOnMissingConverter) {
                throw new TypeConvertException(String.format("Failed to find matching converter from type '%s' to '%s'", objectType.getTypeName(), targetType.getTypeName()));
            } else {
                return null;
            }
        }
        try {
            // Casting to most "generic" type Object must not fail
            // noinspection unchecked
            return converter.get().apply(objectHolder.getObject());
        } catch (Exception e) {
            throw new TypeConvertException(String.format("Failed to convert value to '%s'", targetType.getTypeName()), e);
        }
    }

    private static Type getFromType(TypeConverter<?, ?> typeConverter) {
        return TypeUtils.getTypeArguments(typeConverter.getClass(), TypeConverter.class)
                .get(fromTypeVariable);
    }

    private static Type getToType(TypeConverter<?, ?> typeConverter) {
        return TypeUtils.getTypeArguments(typeConverter.getClass(), TypeConverter.class)
                .get(toTypeVariable);
    }

}
