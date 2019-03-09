package org.blockchainnative.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.blockchainnative.util.SerializationUtil;

import java.io.IOException;
import java.lang.reflect.Method;

/** Serializer for using {@code Method} objects as keys in collections
 *
 * @see org.blockchainnative.util.SerializationUtil
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class MethodKeySerializer extends StdSerializer<Method> {

    public MethodKeySerializer() {
        this(Method.class);
    }

    public MethodKeySerializer(Class<Method> t) {
        super(t);
    }

    @Override
    public void serialize(Method value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeFieldName(SerializationUtil.buildMethodDescription(value));
    }
}
