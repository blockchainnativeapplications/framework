package org.blockchainnative.fabric.typeconverters;

import java.text.NumberFormat;

/**
 * Converts {@code short} values to {@code String}.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class ShortStringConverter extends NumberStringConverter<Short> {

    public ShortStringConverter() {
        super();
    }

    public ShortStringConverter(NumberFormat numberFormat) {
        super(numberFormat);
    }

    @Override
    public String to(Short value) {
        return super.fromNumber(value);
    }

    @Override
    public Short from(String string) {
        return super.fromString(string).shortValue();
    }
}
