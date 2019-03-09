package org.blockchainnative.fabric.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;

import java.io.IOException;

/** Deserializer for {@code ChaincodeEndorsementPolicy} objects
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class ChaincodePolicyDeserializer extends StdDeserializer<ChaincodeEndorsementPolicy> {

    public ChaincodePolicyDeserializer() {
        this(ChaincodeEndorsementPolicy.class);
    }

    public ChaincodePolicyDeserializer(Class<ChaincodeEndorsementPolicy> t) {
        super(t);
    }

    @Override
    public ChaincodeEndorsementPolicy deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        var bytes = p.getBinaryValue();
        if (bytes != null) {
            var policy = new ChaincodeEndorsementPolicy();
            policy.fromBytes(bytes);

            return policy;
        }
        return null;
    }

    private static String getText(TreeNode treeNode, String nodeName) {
        var node = treeNode.get(nodeName);
        if (node instanceof TextNode) {
            return ((TextNode) node).asText();
        }
        return null;
    }

}
