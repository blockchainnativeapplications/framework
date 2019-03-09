package org.blockchainnative.serialization;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import org.blockchainnative.util.SerializationUtil;

import java.io.IOException;

/** Deserializer for collection keys of {@code Method} objects
 *
 * @see org.blockchainnative.util.SerializationUtil
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class MethodKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        return SerializationUtil.parseMethodDescription(key);
    }
}
