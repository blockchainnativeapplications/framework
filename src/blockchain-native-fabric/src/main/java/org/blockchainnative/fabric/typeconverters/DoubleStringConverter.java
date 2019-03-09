package org.blockchainnative.fabric.typeconverters;

import java.text.NumberFormat;

/**
 * Converts {@code double} values to {@code String}.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class DoubleStringConverter extends NumberStringConverter<Double> {

    public DoubleStringConverter() {
        super();
    }

    public DoubleStringConverter(NumberFormat numberFormat) {
        super(numberFormat);
    }

    @Override
    public String to(Double value) {
        return super.fromNumber(value);
    }

    @Override
    public Double from(String string) {
        return super.fromString(string).doubleValue();
    }
}
