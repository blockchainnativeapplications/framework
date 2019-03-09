package org.blockchainnative.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.blockchainnative.util.SerializationUtil;

import java.io.IOException;
import java.lang.reflect.Field;

/** Serializer for {@code Field} objects
 *
 * @see org.blockchainnative.util.SerializationUtil
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class FieldSerializer extends StdSerializer<Field> {

    public FieldSerializer() {
        this(Field.class);
    }

    public FieldSerializer(Class<Field> t) {
        super(t);
    }

    @Override
    public void serialize(Field value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(SerializationUtil.buildFieldDescription(value));
    }
}
