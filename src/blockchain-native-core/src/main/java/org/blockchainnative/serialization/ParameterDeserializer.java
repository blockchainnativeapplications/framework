package org.blockchainnative.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.blockchainnative.util.SerializationUtil;

import java.io.IOException;
import java.lang.reflect.Parameter;

/** Serializer for {@code Parameter} objects
 *
 * @see org.blockchainnative.util.SerializationUtil
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class ParameterDeserializer extends StdDeserializer<Parameter> {

    public ParameterDeserializer() {
        this(Parameter.class);
    }

    public ParameterDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Parameter deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return SerializationUtil.parseParameterDescription(p.getText());
    }
}
