package org.blockchainnative.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.blockchainnative.util.SerializationUtil;

import java.io.IOException;
import java.lang.reflect.Method;

/** Deserializer for {@code Method} objects
 *
 * @see org.blockchainnative.util.SerializationUtil
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class MethodDeserializer extends StdDeserializer<Method> {

    public MethodDeserializer() {
        this(Method.class);
    }

    public MethodDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Method deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return SerializationUtil.parseMethodDescription(p.getText());
    }
}
