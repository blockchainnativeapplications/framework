package org.blockchainnative.fabric.typeconverters;

import java.text.NumberFormat;

/**
 * Converts {@code long} values to {@code String}.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class LongStringConverter extends NumberStringConverter<Long> {

    public LongStringConverter() {
        super();
    }

    public LongStringConverter(NumberFormat numberFormat) {
        super(numberFormat);
    }

    @Override
    public String to(Long value) {
        return super.fromNumber(value);
    }

    @Override
    public Long from(String string) {
        return super.fromString(string).longValue();
    }
}
