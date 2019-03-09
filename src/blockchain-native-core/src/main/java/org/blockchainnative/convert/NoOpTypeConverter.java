package org.blockchainnative.convert;

/** Dummy type converter used as a placeholder meaning to use no specific type converter.
 *  This class is <b>not</b> meant to be used for anything else than acting as a placeholder.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public final class NoOpTypeConverter implements TypeConverter<Object, Object> {

    @Override
    public Object to(Object o) {
        return o;
    }

    @Override
    public Object from(Object o) {
        return o;
    }
}
