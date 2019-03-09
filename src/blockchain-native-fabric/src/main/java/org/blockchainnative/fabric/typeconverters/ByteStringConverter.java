package org.blockchainnative.fabric.typeconverters;

import java.text.NumberFormat;

/**
 * Converts {@code byte} values to {@code String}.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class ByteStringConverter extends NumberStringConverter<Byte> {

    public ByteStringConverter() {
        super();
    }

    public ByteStringConverter(NumberFormat numberFormat) {
        super(numberFormat);
    }

    @Override
    public String to(Byte value) {
        return super.fromNumber(value);
    }

    @Override
    public Byte from(String string) {
        return super.fromString(string).byteValue();
    }
}
