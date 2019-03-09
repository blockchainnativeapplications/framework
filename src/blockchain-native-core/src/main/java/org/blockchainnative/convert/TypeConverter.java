package org.blockchainnative.convert;

/**
 * Converts objects between the types {@code TFrom} and {@code TTo}.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public interface TypeConverter<TFrom, TTo> {

    /**
     * Converts the given object from type {@code TFrom} to {@code TTo}.
     *
     * @param from object to be converted
     * @return converted object
     */
    TTo to(TFrom from);

    /**
     * Converts the given object from type {@code TTo} to {@code TFrom}.
     *
     * @param to object to be converted
     * @return converted object
     */
    TFrom from(TTo to);
}

