package org.blockchainnative.ethereum.test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.blockchainnative.ethereum.builder.EthereumContractInfoBuilder;
import org.blockchainnative.ethereum.metadata.EthereumContractInfo;
import org.blockchainnative.ethereum.metadata.EthereumParameterInfo;
import org.blockchainnative.ethereum.serialization.EthereumMetadataModule;
import org.blockchainnative.ethereum.serialization.EthereumParameterInfoMixin;
import org.blockchainnative.ethereum.test.contracts.EthereumHelloContract;
import org.blockchainnative.ethereum.test.contracts.EthereumHelloContractWithEvent;
import org.blockchainnative.registry.FileSystemContractRegistry;
import org.blockchainnative.serialization.ParameterDeserializer;
import org.blockchainnative.serialization.ParameterSerializer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Matthias Veit
 */
public class SerializationTest {


    @Test
    public void persistHelloContract() throws Exception {
        var contractRegistry = new FileSystemContractRegistry(new File(this.getClass().getClassLoader().getResource("contractInfos/ethereum").getFile()).toPath())
                .registerObjectMapperModule(new EthereumMetadataModule());

        var helloWorldContractInfo = getHelloContractContractInfo();

        contractRegistry.addContractInfo(helloWorldContractInfo);
        contractRegistry.persist();

        var serializedFileName = this.getClass().getClassLoader().getResource("contractInfos/ethereum/" + helloWorldContractInfo.getIdentifier() + ".json").getFile();
        var serializedContent = new String(Files.readAllBytes(new File(serializedFileName).toPath()), StandardCharsets.UTF_8);

        // replace line separators and white space as they do not matter
        serializedContent = EthereumContractInfo.normalizeAbiString(serializedContent);

        assertEquals(getExpectedHelloContractString(), serializedContent);
    }

    @Test
    public void loadHelloContract() throws Exception {
        var contractRegistry = new FileSystemContractRegistry(new File(this.getClass().getClassLoader().getResource("contractInfos/ethereum").getFile()).toPath())
                .registerObjectMapperModule(new EthereumMetadataModule());

        contractRegistry.load();

        var actual = contractRegistry.getContractInfo("cc87f2322b6e4054b923daca9557dcc8");

        assertNotNull(actual);
        assertEquals(getHelloContractContractInfo(), actual);
    }

    @Test
    public void persistHelloContractWithEvents() throws Exception {
        var contractRegistry = new FileSystemContractRegistry(new File(this.getClass().getClassLoader().getResource("contractInfos/ethereum").getFile()).toPath())
                .registerObjectMapperModule(new EthereumMetadataModule());

        var helloWorldContractInfo = getHelloContractWithEventsContractInfo();

        contractRegistry.addContractInfo(helloWorldContractInfo);
        contractRegistry.persist();

        var serializedFileName = this.getClass().getClassLoader().getResource("contractInfos/ethereum/" + helloWorldContractInfo.getIdentifier() + ".json").getFile();
        var serializedContent = new String(Files.readAllBytes(new File(serializedFileName).toPath()), StandardCharsets.UTF_8);
        // replace line separators and white space as they do not matter
        serializedContent = EthereumContractInfo.normalizeAbiString(serializedContent);

        assertEquals(getExpectedHelloContractWithEventsString(), serializedContent);
    }

    @Test
    public void loadHelloContractWithEvents() throws Exception {
        var contractRegistry = new FileSystemContractRegistry(new File(this.getClass().getClassLoader().getResource("contractInfos/ethereum").getFile()).toPath())
                .registerObjectMapperModule(new EthereumMetadataModule());

        contractRegistry.load();

        var actual = contractRegistry.getContractInfo("a292c230456f4adc92f111ef6b9b6cc1");

        assertNotNull(actual);
        assertEquals(getHelloContractWithEventsContractInfo(), actual);
    }

    @Test
    public void serializeEthereumParameterInfo() throws Exception{
        var parameter = String.class.getMethod("charAt", int.class).getParameters()[0];
        var parameterInfo = new EthereumParameterInfo(parameter, 0, "int", null, String.class, null);

        var objectMapper = new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new SimpleModule().addSerializer(Parameter.class, new ParameterSerializer()));

        var serialized = objectMapper.writeValueAsString(parameterInfo);

        var parameterInfoAsString = "{\"parameter\":\"java.lang.String.charAt(int)[0]\",\"parameterIndex\":0,\"typeConverterClass\":null,\"passParameterAsType\":\"java.lang.String\",\"specialArgumentName\":null,\"solidityType\":\"int\",\"specialArgument\":false}";

        assertEquals(parameterInfoAsString, serialized);
    }

    @Test
    public void deserializeEthereumParameterInfo() throws Exception {
        var parameter = String.class.getMethod("charAt", int.class).getParameters()[0];
        var parameterInfo = new EthereumParameterInfo(parameter, 0, "int", null, String.class, null);
        var parameterInfoAsString = "{\"parameter\":\"java.lang.String.charAt(int)[0]\",\"parameterIndex\":0,\"typeConverterClass\":null,\"passParameterAsType\":\"java.lang.String\",\"specialArgumentName\":null,\"solidityType\":\"int\",\"specialArgument\":false}";

        var objectMapper = new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(
                        new SimpleModule()
                                .addDeserializer(Parameter.class, new ParameterDeserializer())
                                .setMixInAnnotation(EthereumParameterInfo.class, EthereumParameterInfoMixin.class))
                .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));

        var deserialized = objectMapper.readValue(parameterInfoAsString, EthereumParameterInfo.class);

        assertEquals(parameterInfo, deserialized);
    }

    private EthereumContractInfo<EthereumHelloContract> getHelloContractContractInfo() throws IOException {
        return new EthereumContractInfoBuilder<>(EthereumHelloContract.class)
                .withIdentifier("cc87f2322b6e4054b923daca9557dcc8")
                .atAddress("0xdeadbeef")
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/HelloWorld.abi").getFile()))
                .withBinary(new File(this.getClass().getClassLoader().getResource("contracts/compiled/HelloWorld.bin").getFile()))
                .build();
    }

    private EthereumContractInfo<EthereumHelloContractWithEvent> getHelloContractWithEventsContractInfo() throws IOException {
        return new EthereumContractInfoBuilder<>(EthereumHelloContractWithEvent.class)
                .withIdentifier("a292c230456f4adc92f111ef6b9b6cc1")
                .atAddress("0xdeadbeef")
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/HelloWorldWithEvents.abi").getFile()))
                .withBinary(new File(this.getClass().getClassLoader().getResource("contracts/compiled/HelloWorldWithEvents.bin").getFile()))
                .build();
    }

    private String getExpectedHelloContractString(){
        var expected = "[\n" +
                "  \"org.blockchainnative.ethereum.metadata.EthereumContractInfo\",\n" +
                "  {\n" +
                "    \"abi\": \"[{\\\"constant\\\":true,\\\"inputs\\\":[{\\\"name\\\":\\\"greeter\\\",\\\"type\\\":\\\"string\\\"}],\\\"name\\\":\\\"hello\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"string\\\"}],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"view\\\",\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[{\\\"name\\\":\\\"g\\\",\\\"type\\\":\\\"string\\\"}],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"constructor\\\"}]\",\n" +
                "    \"binary\": \"608060405234801561001057600080fd5b5060405161037a38038061037a833981018060405281019080805182019291905050508060009080519060200190610049929190610050565b50506100f5565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061009157805160ff19168380011785556100bf565b828001600101855582156100bf579182015b828111156100be5782518255916020019190600101906100a3565b5b5090506100cc91906100d0565b5090565b6100f291905b808211156100ee5760008160009055506001016100d6565b5090565b90565b610276806101046000396000f300608060405260043610610041576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063a777d0dc14610046575b600080fd5b34801561005257600080fd5b506100ad600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050610128565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156100ed5780820151818401526020810190506100d2565b50505050905090810190601f16801561011a5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6060600082604051602001808380546001816001161561010002031660029004801561018b5780601f1061016957610100808354040283529182019161018b565b820191906000526020600020905b815481529060010190602001808311610177575b5050807f200000000000000000000000000000000000000000000000000000000000000081525060010182805190602001908083835b6020831015156101e657805182526020820191506020810190506020830392506101c1565b6001836020036101000a038019825116818451168082178552505050505050905001807f21000000000000000000000000000000000000000000000000000000000000008152506001019250505060405160208183030381529060405290509190505600a165627a7a72305820bd998bfe4b71da4b4b3f2537ea6fafc891ccc1564549672bc1c1dbe23bcc6bb30029\",\n" +
                "    \"contractAddress\": \"0xdeadbeef\",\n" +
                "    \"contractClass\": \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContract\",\n" +
                "    \"eventInfos\": {},\n" +
                "    \"identifier\": \"cc87f2322b6e4054b923daca9557dcc8\",\n" +
                "    \"methodInfos\": {\n" +
                "      \"org.blockchainnative.test.contracts.HelloContract.hello(java.lang.String)\": {\n" +
                "        \"abi\": {\n" +
                "          \"constant\": true,\n" +
                "          \"inputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"greeter\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"name\": \"hello\",\n" +
                "          \"outputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"payable\": false,\n" +
                "          \"stateMutability\": \"view\",\n" +
                "          \"type\": \"function\"\n" +
                "        },\n" +
                "        \"contractMethodName\": \"hello\",\n" +
                "        \"method\": \"org.blockchainnative.test.contracts.HelloContract.hello(java.lang.String)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.test.contracts.HelloContract.hello(java.lang.String)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": \"string\",\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": false,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": false\n" +
                "      },\n" +
                "      \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContract.deploy(java.math.BigInteger,java.math.BigInteger,java.lang.String)\": {\n" +
                "        \"abi\": {\n" +
                "          \"constant\": false,\n" +
                "          \"inputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"g\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"name\": null,\n" +
                "          \"outputs\": null,\n" +
                "          \"payable\": false,\n" +
                "          \"stateMutability\": \"nonpayable\",\n" +
                "          \"type\": \"constructor\"\n" +
                "        },\n" +
                "        \"contractMethodName\": \"deploy\",\n" +
                "        \"method\": \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContract.deploy(java.math.BigInteger,java.math.BigInteger,java.lang.String)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContract.deploy(java.math.BigInteger,java.math.BigInteger,java.lang.String)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": null,\n" +
                "            \"specialArgumentName\": \"gasPrice\",\n" +
                "            \"typeConverterClass\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContract.deploy(java.math.BigInteger,java.math.BigInteger,java.lang.String)[1]\",\n" +
                "            \"parameterIndex\": 1,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": null,\n" +
                "            \"specialArgumentName\": \"gasLimit\",\n" +
                "            \"typeConverterClass\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContract.deploy(java.math.BigInteger,java.math.BigInteger,java.lang.String)[2]\",\n" +
                "            \"parameterIndex\": 2,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": \"string\",\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": false,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": true\n" +
                "      },\n" +
                "      \"org.blockchainnative.test.contracts.HelloContract.helloReadOnly(java.lang.String)\": {\n" +
                "        \"abi\": {\n" +
                "          \"constant\": true,\n" +
                "          \"inputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"greeter\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"name\": \"hello\",\n" +
                "          \"outputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"payable\": false,\n" +
                "          \"stateMutability\": \"view\",\n" +
                "          \"type\": \"function\"\n" +
                "        },\n" +
                "        \"contractMethodName\": \"hello\",\n" +
                "        \"method\": \"org.blockchainnative.test.contracts.HelloContract.helloReadOnly(java.lang.String)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.test.contracts.HelloContract.helloReadOnly(java.lang.String)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": \"string\",\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": true,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": false\n" +
                "      },\n" +
                "      \"org.blockchainnative.test.contracts.HelloContract.helloAsync(java.lang.String)\": {\n" +
                "        \"abi\": {\n" +
                "          \"constant\": true,\n" +
                "          \"inputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"greeter\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"name\": \"hello\",\n" +
                "          \"outputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"payable\": false,\n" +
                "          \"stateMutability\": \"view\",\n" +
                "          \"type\": \"function\"\n" +
                "        },\n" +
                "        \"contractMethodName\": \"hello\",\n" +
                "        \"method\": \"org.blockchainnative.test.contracts.HelloContract.helloAsync(java.lang.String)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.test.contracts.HelloContract.helloAsync(java.lang.String)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": \"string\",\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": false,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": false\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "]";
        return EthereumContractInfo.normalizeAbiString(expected);
    }

    private String getExpectedHelloContractWithEventsString(){
        var expected = "[\n" +
                "  \"org.blockchainnative.ethereum.metadata.EthereumContractInfo\",\n" +
                "  {\n" +
                "    \"abi\": \"[{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"greeter\\\",\\\"type\\\":\\\"string\\\"}],\\\"name\\\":\\\"hello\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"string\\\"}],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[{\\\"name\\\":\\\"g\\\",\\\"type\\\":\\\"string\\\"}],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"constructor\\\"},{\\\"anonymous\\\":false,\\\"inputs\\\":[{\\\"indexed\\\":false,\\\"name\\\":\\\"name\\\",\\\"type\\\":\\\"string\\\"}],\\\"name\\\":\\\"greeted\\\",\\\"type\\\":\\\"event\\\"}]\",\n" +
                "    \"binary\": \"608060405234801561001057600080fd5b50604051610416380380610416833981018060405281019080805182019291905050508060009080519060200190610049929190610050565b50506100f5565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061009157805160ff19168380011785556100bf565b828001600101855582156100bf579182015b828111156100be5782518255916020019190600101906100a3565b5b5090506100cc91906100d0565b5090565b6100f291905b808211156100ee5760008160009055506001016100d6565b5090565b90565b610312806101046000396000f300608060405260043610610041576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063a777d0dc14610046575b600080fd5b34801561005257600080fd5b506100ad600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050610128565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156100ed5780820151818401526020810190506100d2565b50505050905090810190601f16801561011a5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b60607ff356444d694c95016627e6869f806b9fa7abb9d09573b0f310fdee8d57fe6036826040518080602001828103825283818151815260200191508051906020019080838360005b8381101561018c578082015181840152602081019050610171565b50505050905090810190601f1680156101b95780820380516001836020036101000a031916815260200191505b509250505060405180910390a160008260405160200180838054600181600116156101000203166002900480156102275780601f10610205576101008083540402835291820191610227565b820191906000526020600020905b815481529060010190602001808311610213575b5050807f200000000000000000000000000000000000000000000000000000000000000081525060010182805190602001908083835b602083101515610282578051825260208201915060208101905060208303925061025d565b6001836020036101000a038019825116818451168082178552505050505050905001807f21000000000000000000000000000000000000000000000000000000000000008152506001019250505060405160208183030381529060405290509190505600a165627a7a72305820a1a7da1c49576a022d2ece49fca5bb43be30359774c5bcdcdc7410b3bffbfb710029\",\n" +
                "    \"contractAddress\": \"0xdeadbeef\",\n" +
                "    \"contractClass\": \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContractWithEvent\",\n" +
                "    \"eventInfos\": {\n" +
                "      \"greeted\": {\n" +
                "        \"abiDefinition\": {\n" +
                "          \"constant\": false,\n" +
                "          \"inputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"name\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"name\": \"greeted\",\n" +
                "          \"outputs\": null,\n" +
                "          \"payable\": false,\n" +
                "          \"stateMutability\": null,\n" +
                "          \"type\": \"event\"\n" +
                "        },\n" +
                "        \"eventFieldInfos\": [\n" +
                "          {\n" +
                "            \"field\": \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContractWithEvent.HelloEvent.name\",\n" +
                "            \"solidityType\": {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"name\",\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"sourceFieldIndex\": null,\n" +
                "            \"sourceFieldName\": \"name\",\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"eventName\": \"greeted\",\n" +
                "        \"eventParameterInfos\": [],\n" +
                "        \"method\": \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContractWithEvent.onGreeted()\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"identifier\": \"a292c230456f4adc92f111ef6b9b6cc1\",\n" +
                "    \"methodInfos\": {\n" +
                "      \"org.blockchainnative.test.contracts.HelloContract.hello(java.lang.String)\": {\n" +
                "        \"abi\": {\n" +
                "          \"constant\": false,\n" +
                "          \"inputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"greeter\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"name\": \"hello\",\n" +
                "          \"outputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"payable\": false,\n" +
                "          \"stateMutability\": \"nonpayable\",\n" +
                "          \"type\": \"function\"\n" +
                "        },\n" +
                "        \"contractMethodName\": \"hello\",\n" +
                "        \"method\": \"org.blockchainnative.test.contracts.HelloContract.hello(java.lang.String)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.test.contracts.HelloContract.hello(java.lang.String)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": \"string\",\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": false,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": false\n" +
                "      },\n" +
                "      \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContract.deploy(java.math.BigInteger,java.math.BigInteger,java.lang.String)\": {\n" +
                "        \"abi\": {\n" +
                "          \"constant\": false,\n" +
                "          \"inputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"g\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"name\": null,\n" +
                "          \"outputs\": null,\n" +
                "          \"payable\": false,\n" +
                "          \"stateMutability\": \"nonpayable\",\n" +
                "          \"type\": \"constructor\"\n" +
                "        },\n" +
                "        \"contractMethodName\": \"deploy\",\n" +
                "        \"method\": \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContract.deploy(java.math.BigInteger,java.math.BigInteger,java.lang.String)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContract.deploy(java.math.BigInteger,java.math.BigInteger,java.lang.String)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": null,\n" +
                "            \"specialArgumentName\": \"gasPrice\",\n" +
                "            \"typeConverterClass\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContract.deploy(java.math.BigInteger,java.math.BigInteger,java.lang.String)[1]\",\n" +
                "            \"parameterIndex\": 1,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": null,\n" +
                "            \"specialArgumentName\": \"gasLimit\",\n" +
                "            \"typeConverterClass\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.ethereum.test.contracts.EthereumHelloContract.deploy(java.math.BigInteger,java.math.BigInteger,java.lang.String)[2]\",\n" +
                "            \"parameterIndex\": 2,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": \"string\",\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": false,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": true\n" +
                "      },\n" +
                "      \"org.blockchainnative.test.contracts.HelloContract.helloReadOnly(java.lang.String)\": {\n" +
                "        \"abi\": {\n" +
                "          \"constant\": false,\n" +
                "          \"inputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"greeter\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"name\": \"hello\",\n" +
                "          \"outputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"payable\": false,\n" +
                "          \"stateMutability\": \"nonpayable\",\n" +
                "          \"type\": \"function\"\n" +
                "        },\n" +
                "        \"contractMethodName\": \"hello\",\n" +
                "        \"method\": \"org.blockchainnative.test.contracts.HelloContract.helloReadOnly(java.lang.String)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.test.contracts.HelloContract.helloReadOnly(java.lang.String)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": \"string\",\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": true,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": false\n" +
                "      },\n" +
                "      \"org.blockchainnative.test.contracts.HelloContract.helloAsync(java.lang.String)\": {\n" +
                "        \"abi\": {\n" +
                "          \"constant\": false,\n" +
                "          \"inputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"greeter\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"name\": \"hello\",\n" +
                "          \"outputs\": [\n" +
                "            {\n" +
                "              \"indexed\": false,\n" +
                "              \"name\": \"\",\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"payable\": false,\n" +
                "          \"stateMutability\": \"nonpayable\",\n" +
                "          \"type\": \"function\"\n" +
                "        },\n" +
                "        \"contractMethodName\": \"hello\",\n" +
                "        \"method\": \"org.blockchainnative.test.contracts.HelloContract.helloAsync(java.lang.String)\",\n" +
                "        \"parameterInfos\": [\n" +
                "          {\n" +
                "            \"parameter\": \"org.blockchainnative.test.contracts.HelloContract.helloAsync(java.lang.String)[0]\",\n" +
                "            \"parameterIndex\": 0,\n" +
                "            \"passParameterAsType\": null,\n" +
                "            \"solidityType\": \"string\",\n" +
                "            \"specialArgumentName\": null,\n" +
                "            \"typeConverterClass\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"readOnly\": false,\n" +
                "        \"resultTypeConverterClass\": null,\n" +
                "        \"specialMethod\": false\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "]";
        return EthereumContractInfo.normalizeAbiString(expected);
    }

}
