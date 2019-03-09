package org.blockchainnative.fabric.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.hyperledger.fabric.sdk.ChaincodeID;

import java.io.IOException;

/** Serializer for {@code ChaincodeID} objects
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class ChaincodeIDSerializer extends StdSerializer<ChaincodeID> {

    public ChaincodeIDSerializer() {
        this(ChaincodeID.class);
    }

    public ChaincodeIDSerializer(Class<ChaincodeID> t) {
        super(t);
    }

    @Override
    public void serialize(ChaincodeID value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", value.getName());
        gen.writeStringField("version", value.getVersion());
        gen.writeStringField("path", value.getPath());
        gen.writeEndObject();
    }
}
