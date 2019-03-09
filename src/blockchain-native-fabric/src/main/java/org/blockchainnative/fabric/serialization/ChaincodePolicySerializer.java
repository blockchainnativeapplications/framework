package org.blockchainnative.fabric.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;

import java.io.IOException;

/** Serializer for {@code ChaincodeEndorsementPolicy} objects
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class ChaincodePolicySerializer extends StdSerializer<ChaincodeEndorsementPolicy> {

    public ChaincodePolicySerializer() {
        this(ChaincodeEndorsementPolicy.class);
    }

    public ChaincodePolicySerializer(Class<ChaincodeEndorsementPolicy> t) {
        super(t);
    }

    @Override
    public void serialize(ChaincodeEndorsementPolicy value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeBinary(value.getChaincodeEndorsementPolicyAsBytes());
    }
}
