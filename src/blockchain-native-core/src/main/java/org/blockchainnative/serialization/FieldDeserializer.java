package org.blockchainnative.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.blockchainnative.util.SerializationUtil;

import java.io.IOException;
import java.lang.reflect.Field;

/** Deserializer for {@code Field} objects
 *
 * @see org.blockchainnative.util.SerializationUtil
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class FieldDeserializer extends StdDeserializer<Field> {

    public FieldDeserializer() {
        this(Field.class);
    }

    public FieldDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Field deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return SerializationUtil.parseFieldDescription(p.getText());
    }
}
