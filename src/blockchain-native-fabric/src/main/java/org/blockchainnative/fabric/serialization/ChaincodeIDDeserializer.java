package org.blockchainnative.fabric.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import org.hyperledger.fabric.sdk.ChaincodeID;

import java.io.IOException;

/** Deserializer for {@code ChaincodeID} objects
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class ChaincodeIDDeserializer extends StdDeserializer<ChaincodeID> {

    public ChaincodeIDDeserializer() {
        this(ChaincodeID.class);
    }

    public ChaincodeIDDeserializer(Class<ChaincodeID> t) {
        super(t);
    }

    @Override
    public ChaincodeID deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        var node = p.getCodec().readTree(p);

        var name = getText(node, "name");
        var version = getText(node, "version");
        var path = getText(node, "path");

        return ChaincodeID.newBuilder()
                .setName(name)
                .setVersion(version)
                .setPath(path)
                .build();
    }

    private static String getText(TreeNode treeNode, String nodeName) {
        var node = treeNode.get(nodeName);
        if(node instanceof TextNode){
            return ((TextNode) node).asText();
        }
        return null;
    }

}
