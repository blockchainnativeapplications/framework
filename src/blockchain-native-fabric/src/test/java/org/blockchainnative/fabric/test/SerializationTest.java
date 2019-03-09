package org.blockchainnative.fabric.test;

import org.blockchainnative.fabric.metadata.FabricContractInfo;
import org.blockchainnative.fabric.serialization.FabricMetadataModule;
import org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations;
import org.blockchainnative.registry.FileSystemContractRegistry;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Matthias Veit
 */
public class SerializationTest {

    @Test
    public void serializeHelloContractWithoutAnnotations() throws IOException, ChaincodeEndorsementPolicyParseException, URISyntaxException {
        var contractInfo = ContractBuilderTest.getHelloContractWithoutAnnotationsContractInfo("4fe31e8062d443368023b8d79cb320a2", ChaincodeID.newBuilder().setName("hello").setVersion("1.0").setPath("hello").build());
        var contractRegistry = new FileSystemContractRegistry(new File(this.getClass().getClassLoader().getResource("contractInfos").getFile()).toPath())
                .registerObjectMapperModule(new FabricMetadataModule());

        contractRegistry.addContractInfo(contractInfo);
        contractRegistry.persist();

        var serializedFileName = this.getClass().getClassLoader().getResource("contractInfos/" + contractInfo.getIdentifier() + ".json").getFile();
        var serializedContent = new String(Files.readAllBytes(new File(serializedFileName).toPath()), StandardCharsets.UTF_8);

        Assert.assertEquals(getExpectedHelloContractWithoutAnnotationsContractInfoString(), normalizeJson(serializedContent));
    }

    @Test
    public void deserializeHelloContractWithoutAnnotations() throws URISyntaxException, IOException {
        var contractRegistry = new FileSystemContractRegistry(new File(this.getClass().getClassLoader().getResource("contractInfos").getFile()).toPath())
                .registerObjectMapperModule(new FabricMetadataModule());

        contractRegistry.load();

        var actual = contractRegistry.getContractInfo("4fe31e8062d443368023b8d79cb320a2");

        assertNotNull(actual);
        assertEquals(getExpectedHelloContractWithoutAnnotationsContractInfo(), actual);
    }

    public static FabricContractInfo<FabricHelloContractWithoutAnnotations> getExpectedHelloContractWithoutAnnotationsContractInfo() {
        try {
            return ContractBuilderTest.getHelloContractWithoutAnnotationsContractInfo("4fe31e8062d443368023b8d79cb320a2", ChaincodeID.newBuilder().setName("hello").setVersion("1.0").setPath("hello").build());
        } catch (IOException | ChaincodeEndorsementPolicyParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getExpectedHelloContractWithoutAnnotationsContractInfoString() {
        return normalizeJson("[\n" +
                "  \"org.blockchainnative.fabric.metadata.FabricContractInfo\",\n" +
                "  {\n" +
                "    \"chaincodeID\": {\n" +
                "      \"name\": \"hello\",\n" +
                "      \"version\": \"1.0\",\n" +
                "      \"path\": \"hello\"\n" +
                "    },\n" +
                "    \"chaincodeLanguage\": \"Go\",\n" +
                "    \"chaincodePolicy\": \"EiASHggBEgwSCggBEgIIABICCAISDBIKCAESAggBEgIIAxoMEgoKBkZvb01TUBADGgwSCgoGQmFyTVNQEAMaDBIKCgZGb29NU1AQARoMEgoKBkJhck1TUBAB\",\n" +
                "    \"chaincodeSourceDirectory\": \"chaincode/hello\",\n" +
                "    \"contractClass\": \"org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations\",\n" +
                "    \"eventInfos\": {\n" +
                "      \"greeted\": {\n" +
                "        \"eventFieldInfos\": [\n" +
                "          {\n" +
                "            \"field\": \"org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations.HelloEvent.name\",\n" +
                "            \"sourceFieldIndex\": null,\n" +
                "            \"sourceFieldName\": \"payload\",\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"eventName\": \"greeted\",\n" +
                "        \"eventParameterInfos\": [],\n" +
                "        \"method\": \"org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations.onGreeted()\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"identifier\": \"4fe31e8062d443368023b8d79cb320a2\",\n" +
                "    \"methodInfos\": {\n" +
                "      \"org.blockchainnative.test.contracts.HelloContractWithoutAnnotations.helloReadOnly(java.lang.String)\": {\n" +
                "        \"contractMethodName\": \"hello\",\n" +
                "        \"method\": \"org.blockchainnative.test.contracts.HelloContractWithoutAnnotations.helloReadOnly(java.lang.String)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.test.contracts.HelloContractWithoutAnnotations.helloReadOnly(java.lang.String)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": \"java.lang.String\",\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": true,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": false\n" +
                "      },\n" +
                "      \"org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations.instantiate(java.lang.String,java.util.Set,org.hyperledger.fabric.sdk.User)\": {\n" +
                "        \"contractMethodName\": \"instantiate\",\n" +
                "        \"method\": \"org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations.instantiate(java.lang.String,java.util.Set,org.hyperledger.fabric.sdk.User)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations.instantiate(java.lang.String,java.util.Set,org.hyperledger.fabric.sdk.User)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations.instantiate(java.lang.String,java.util.Set,org.hyperledger.fabric.sdk.User)[1]\",\n" +
                "            \"parameterIndex\": 1,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"specialArgumentName\": \"targetPeers\",\n" +
                "            \"typeConverterClass\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations.instantiate(java.lang.String,java.util.Set,org.hyperledger.fabric.sdk.User)[2]\",\n" +
                "            \"parameterIndex\": 2,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"specialArgumentName\": \"user\",\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": false,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": true\n" +
                "      },\n" +
                "      \"org.blockchainnative.test.contracts.HelloContractWithoutAnnotations.helloAsync(java.lang.String)\": {\n" +
                "        \"contractMethodName\": \"hello\",\n" +
                "        \"method\": \"org.blockchainnative.test.contracts.HelloContractWithoutAnnotations.helloAsync(java.lang.String)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.test.contracts.HelloContractWithoutAnnotations.helloAsync(java.lang.String)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": false,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": false\n" +
                "      },\n" +
                "      \"org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations.install(java.util.Set,org.hyperledger.fabric.sdk.User)\": {\n" +
                "        \"contractMethodName\": \"install\",\n" +
                "        \"method\": \"org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations.install(java.util.Set,org.hyperledger.fabric.sdk.User)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations.install(java.util.Set,org.hyperledger.fabric.sdk.User)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"specialArgumentName\": \"targetPeers\",\n" +
                "            \"typeConverterClass\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations.install(java.util.Set,org.hyperledger.fabric.sdk.User)[1]\",\n" +
                "            \"parameterIndex\": 1,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"specialArgumentName\": \"user\",\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": false,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": true\n" +
                "      },\n" +
                "      \"org.blockchainnative.test.contracts.HelloContractWithoutAnnotations.hello(java.lang.String)\": {\n" +
                "        \"contractMethodName\": \"hello\",\n" +
                "        \"method\": \"org.blockchainnative.test.contracts.HelloContractWithoutAnnotations.hello(java.lang.String)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.test.contracts.HelloContractWithoutAnnotations.hello(java.lang.String)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": false,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": false\n" +
                "      }\n" +
                "    },\n" +
                "    \"targetPeerNames\": [\n" +
                "      \"peer0.foo.bcn.org\",\n" +
                "      \"peer0.bar.bcn.org\"\n" +
                "    ],\n" +
                "    \"installedOnPeers\":null," +
                "    \"instantiatedOnPeers\":null" +
                "  }\n" +
                "]\n");
    }

    private static String normalizeJson(String json) {
        return json.replaceAll("\r?\n", "")
                .replaceAll("\\h", "");
    }
}
