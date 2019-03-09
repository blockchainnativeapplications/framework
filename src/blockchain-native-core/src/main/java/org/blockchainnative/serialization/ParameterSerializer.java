package org.blockchainnative.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.blockchainnative.util.SerializationUtil;

import java.io.IOException;
import java.lang.reflect.Parameter;

/** Deserializer for {@code Parameter} objects
 *
 * @see org.blockchainnative.util.SerializationUtil
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class ParameterSerializer extends StdSerializer<Parameter> {

    public ParameterSerializer() {
        this(Parameter.class);
    }

    public ParameterSerializer(Class<Parameter> t) {
        super(t);
    }

    @Override
    public void serialize(Parameter value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(SerializationUtil.buildParameterDescription(value));
    }

}
