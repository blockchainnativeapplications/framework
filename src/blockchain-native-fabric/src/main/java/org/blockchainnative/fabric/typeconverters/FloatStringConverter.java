package org.blockchainnative.fabric.typeconverters;

import java.text.NumberFormat;

/**
 * Converts {@code float} values to {@code String}.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class FloatStringConverter extends NumberStringConverter<Float> {

    public FloatStringConverter() {
        super();
    }

    public FloatStringConverter(NumberFormat numberFormat) {
        super(numberFormat);
    }

    @Override
    public String to(Float value) {
        return super.fromNumber(value);
    }

    @Override
    public Float from(String string) {
        return super.fromString(string).floatValue();
    }
}
