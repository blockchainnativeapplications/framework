package org.blockchainnative.fabric.typeconverters;

import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.exceptions.TypeConvertException;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Abstract base class for converters which convert subtypes of {@code Number} to {@code String}.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public abstract class NumberStringConverter<TNumber extends Number> implements TypeConverter<TNumber, String> {

    private final NumberFormat numberFormat;

    public NumberStringConverter() {
        this.numberFormat = NumberFormat.getInstance(Locale.getDefault());
    }

    public NumberStringConverter(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    protected String fromNumber(TNumber number) {
        return numberFormat.format(number);
    }

    protected Number fromString(String s) {
        try {
            return numberFormat.parse(s);
        } catch (ParseException e) {
            throw new TypeConvertException(String.format("Failed to convert '%s' to Number.", s), e);
        }
    }
}
