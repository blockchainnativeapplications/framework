package org.blockchainnative.quorum.test;

import org.blockchainnative.quorum.builder.QuorumContractInfoBuilder;
import org.blockchainnative.quorum.metadata.QuorumContractInfo;
import org.blockchainnative.quorum.serialization.QuorumMetadataModule;
import org.blockchainnative.quorum.test.contracts.SimpleStorageContract;
import org.blockchainnative.registry.FileSystemContractRegistry;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 * @author Matthias Veit
 */
public class SerializationTest {

    @Test
    public void persistSimpleStorageContract() throws Exception {
        var contractRegistry = new FileSystemContractRegistry(new File(this.getClass().getClassLoader().getResource("contractInfos/quorum").getFile()).toPath())
                .registerObjectMapperModule(new QuorumMetadataModule());

        var helloWorldContractInfo = getSimpleStorageContractInfo();

        contractRegistry.addContractInfo(helloWorldContractInfo);
        contractRegistry.persist();

        var serializedFileName = this.getClass().getClassLoader().getResource("contractInfos/quorum/" + helloWorldContractInfo.getIdentifier() + ".json").getFile();
        var serializedContent = new String(Files.readAllBytes(new File(serializedFileName).toPath()), StandardCharsets.UTF_8);

        // replace line separators and white space as they do not matter
        serializedContent = QuorumContractInfo.normalizeAbiString(serializedContent);

        assertEquals(getSimpleStorageContractInfoString(), serializedContent);
    }

    @Test
    public void loadSimpleStorageContract() throws Exception {
        var contractRegistry = new FileSystemContractRegistry(new File(this.getClass().getClassLoader().getResource("contractInfos/quorum").getFile()).toPath())
                .registerObjectMapperModule(new QuorumMetadataModule());

        contractRegistry.load();

        var actual = contractRegistry.getContractInfo("2417b8dc0c354f29a85dd9fae1f35ce6");

        assertNotNull(actual);
        assertEquals(getSimpleStorageContractInfo(), actual);
    }

    private QuorumContractInfo<SimpleStorageContract> getSimpleStorageContractInfo() throws IOException {
        return new QuorumContractInfoBuilder<>(SimpleStorageContract.class)
                .withIdentifier("2417b8dc0c354f29a85dd9fae1f35ce6")
                .atAddress("0xdeadbeef")
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/SimpleStorage.abi").getFile()))
                .withBinary(new File(this.getClass().getClassLoader().getResource("contracts/compiled/SimpleStorage.bin").getFile()))
                .addPrivateRecipient("eVYjl9lnwYeiUQvnquvvA/lQEdyMW0kjpeCKDUzPB1E=")
                .build();
    }

    private String getSimpleStorageContractInfoString(){
        var expected = "[\n" +
                "  \"org.blockchainnative.quorum.metadata.QuorumContractInfo\",\n" +
                "  {\n" +
                "    \"abi\": \"[{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"get\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"int256\\\"}],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"view\\\",\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"value\\\",\\\"type\\\":\\\"int256\\\"}],\\\"name\\\":\\\"set\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"constructor\\\"}]\",\n" +
                "    \"binary\": \"608060405234801561001057600080fd5b506000808190555060df806100266000396000f3006080604052600436106049576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680636d4ce63c14604e578063e5c19b2d146076575b600080fd5b348015605957600080fd5b50606060a0565b6040518082815260200191505060405180910390f35b348015608157600080fd5b50609e6004803603810190808035906020019092919050505060a9565b005b60008054905090565b80600081905550505600a165627a7a72305820918c185c5fce38a14f47056e9fe74eebfa08b0be2c0f38400a1063f86a59dac00029\",\n" +
                "    \"contractAddress\": \"0xdeadbeef\",\n" +
                "    \"contractClass\": \"org.blockchainnative.quorum.test.contracts.SimpleStorageContract\",\n" +
                "    \"eventInfos\": {},\n" +
                "    \"identifier\": \"2417b8dc0c354f29a85dd9fae1f35ce6\",\n" +
                "    \"methodInfos\": {\n" +
                "      \"org.blockchainnative.quorum.test.contracts.SimpleStorageContract.set(int,java.util.List)\": {\n" +
                "        \"abi\": {\n" +
                "          \"constant\": false,\n" +
                "          \"inputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"value\",\n" +
                "              \"type\": \"int256\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"name\": \"set\",\n" +
                "          \"outputs\": [],\n" +
                "          \"payable\": false,\n" +
                "          \"stateMutability\": \"nonpayable\",\n" +
                "          \"type\": \"function\"\n" +
                "        },\n" +
                "        \"contractMethodName\": \"set\",\n" +
                "        \"method\": \"org.blockchainnative.quorum.test.contracts.SimpleStorageContract.set(int,java.util.List)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.quorum.test.contracts.SimpleStorageContract.set(int,java.util.List)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": \"int256\",\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.quorum.test.contracts.SimpleStorageContract.set(int,java.util.List)[1]\",\n" +
                "            \"parameterIndex\": 1,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": null,\n" +
                "            \"specialArgumentName\": \"privateFor\",\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": false,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": false\n" +
                "      },\n" +
                "      \"org.blockchainnative.quorum.test.contracts.SimpleStorageContract.get()\": {\n" +
                "        \"abi\": {\n" +
                "          \"constant\": true,\n" +
                "          \"inputs\": [],\n" +
                "          \"name\": \"get\",\n" +
                "          \"outputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"\",\n" +
                "              \"type\": \"int256\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"payable\": false,\n" +
                "          \"stateMutability\": \"view\",\n" +
                "          \"type\": \"function\"\n" +
                "        },\n" +
                "        \"contractMethodName\": \"get\",\n" +
                "        \"method\": \"org.blockchainnative.quorum.test.contracts.SimpleStorageContract.get()\",\n" +
                "        \"parameterInfos\": [],\n" +
                "        \"readOnly\": true,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": false\n" +
                "      },\n" +
                "      \"org.blockchainnative.quorum.test.contracts.SimpleStorageContract.deploy()\": {\n" +
                "        \"abi\": {\n" +
                "          \"constant\": false,\n" +
                "          \"inputs\": [],\n" +
                "          \"name\": null,\n" +
                "          \"outputs\": null,\n" +
                "          \"payable\": false,\n" +
                "          \"stateMutability\": \"nonpayable\",\n" +
                "          \"type\": \"constructor\"\n" +
                "        },\n" +
                "        \"contractMethodName\": \"deploy\",\n" +
                "        \"method\": \"org.blockchainnative.quorum.test.contracts.SimpleStorageContract.deploy()\",\n" +
                "        \"parameterInfos\": [],\n" +
                "        \"readOnly\": false,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": true\n" +
                "      }\n" +
                "    },\n" +
                "    \"privateFor\": [\n" +
                "      \"eVYjl9lnwYeiUQvnquvvA/lQEdyMW0kjpeCKDUzPB1E=\"\n" +
                "    ]\n" +
                "  }\n" +
                "]";
        return QuorumContractInfo.normalizeAbiString(expected);
    }
}
