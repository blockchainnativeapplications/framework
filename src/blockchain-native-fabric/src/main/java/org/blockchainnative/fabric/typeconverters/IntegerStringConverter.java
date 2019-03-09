package org.blockchainnative.fabric.typeconverters;

import java.text.NumberFormat;

/**
 * Converts {@code int} values to {@code String}.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class IntegerStringConverter extends NumberStringConverter<Integer> {

    public IntegerStringConverter() {
        super();
    }

    public IntegerStringConverter(NumberFormat numberFormat) {
        super(numberFormat);
    }

    @Override
    public String to(Integer value) {
        return super.fromNumber(value);
    }

    @Override
    public Integer from(String string) {
        return super.fromString(string).intValue();
    }
}
