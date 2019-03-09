package org.blockchainnative.fabric.typeconverters;

import org.blockchainnative.convert.TypeConverter;

/**
 * Converts {@code boolean} values to {@code String}.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class BooleanStringConverter implements TypeConverter<Boolean, String> {

    @Override
    public String to(Boolean aBoolean) {
        return String.valueOf(aBoolean);
    }

    @Override
    public Boolean from(String s) {
        return Boolean.valueOf(s);
    }
}
