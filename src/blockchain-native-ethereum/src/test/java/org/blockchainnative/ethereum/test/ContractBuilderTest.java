package org.blockchainnative.ethereum.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.blockchainnative.ethereum.builder.EthereumContractInfoBuilder;
import org.blockchainnative.ethereum.metadata.EthereumContractInfo;
import org.blockchainnative.ethereum.metadata.EthereumMethodInfo;
import org.blockchainnative.ethereum.metadata.EthereumParameterInfo;
import org.blockchainnative.test.contracts.HelloContractWithoutAnnotations;
import org.junit.Test;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;


/**
 * @author Matthias Veit
 */
public class ContractBuilderTest {

    @Test
    public void buildHelloWorldContractInfo() throws IOException, NoSuchMethodException {
        EthereumContractInfo<HelloContractWithoutAnnotations> contractInfo = new EthereumContractInfoBuilder<>(HelloContractWithoutAnnotations.class)
                .withIdentifier("id")
                .withAbi(new File(this.getClass().getClassLoader().getResource("contracts/compiled/HelloWorld.abi").getFile()))
                .withBinary(new File(this.getClass().getClassLoader().getResource("contracts/compiled/HelloWorld.bin").getFile()))
                .atAddress("0xSomeAddress")
                .method("hello", String.class)
                    .name("hello")
                    .parameter(0)
                        .build()
                    .build()
                .method("helloAsync", String.class)
                    .name("hello")
                    .build()
                .method("helloReadOnly", String.class)
                    .name("hello")
                    .readonly(true)
                    .build()
                .build();

        var abiString = "[\n" +
                "  {\n" +
                "    \"constant\": true,\n" +
                "    \"inputs\": [\n" +
                "      {\n" +
                "        \"name\": \"greeter\",\n" +
                "        \"type\": \"string\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"name\": \"hello\",\n" +
                "    \"outputs\": [\n" +
                "      {\n" +
                "        \"name\": \"\",\n" +
                "        \"type\": \"string\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"payable\": false,\n" +
                "    \"stateMutability\": \"view\",\n" +
                "    \"type\": \"function\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"inputs\": [\n" +
                "      {\n" +
                "        \"name\": \"g\",\n" +
                "        \"type\": \"string\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"payable\": false,\n" +
                "    \"stateMutability\": \"nonpayable\",\n" +
                "    \"type\": \"constructor\"\n" +
                "  }\n" +
                "]";

        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        var abi = objectMapper.readValue(abiString, AbiDefinition[].class);

        var binary = "608060405234801561001057600080fd5b5060405161037a38038061037a833981018060405281019080805182019291905050508060009080519060200190610049929190610050565b50506100f5565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061009157805160ff19168380011785556100bf565b828001600101855582156100bf579182015b828111156100be5782518255916020019190600101906100a3565b5b5090506100cc91906100d0565b5090565b6100f291905b808211156100ee5760008160009055506001016100d6565b5090565b90565b610276806101046000396000f300608060405260043610610041576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063a777d0dc14610046575b600080fd5b34801561005257600080fd5b506100ad600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050610128565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156100ed5780820151818401526020810190506100d2565b50505050905090810190601f16801561011a5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6060600082604051602001808380546001816001161561010002031660029004801561018b5780601f1061016957610100808354040283529182019161018b565b820191906000526020600020905b815481529060010190602001808311610177575b5050807f200000000000000000000000000000000000000000000000000000000000000081525060010182805190602001908083835b6020831015156101e657805182526020820191506020810190506020830392506101c1565b6001836020036101000a038019825116818451168082178552505050505050905001807f21000000000000000000000000000000000000000000000000000000000000008152506001019250505060405160208183030381529060405290509190505600a165627a7a72305820bd998bfe4b71da4b4b3f2537ea6fafc891ccc1564549672bc1c1dbe23bcc6bb30029";

        var helloMethod = HelloContractWithoutAnnotations.class.getDeclaredMethod("hello", String.class);
        var helloAsyncMethod = HelloContractWithoutAnnotations.class.getDeclaredMethod("helloAsync", String.class);
        var helloReadOnlyMethod = HelloContractWithoutAnnotations.class.getDeclaredMethod("helloReadOnly", String.class);

        var expectedContractInfo = new EthereumContractInfo<>("id", HelloContractWithoutAnnotations.class, new ArrayList<EthereumMethodInfo>() {{
            add(new EthereumMethodInfo(helloMethod, "hello", false, false, new ArrayList<EthereumParameterInfo>() {{
                add(new EthereumParameterInfo(helloMethod.getParameters()[0], 0, "string", null, null, null));
            }}, abi[0], null));
            add(new EthereumMethodInfo(helloAsyncMethod, "hello", false, false, new ArrayList<>() {{
                add(new EthereumParameterInfo(helloAsyncMethod.getParameters()[0], 0,"string", null, null, null));
            }}, abi[0], null));
            add(new EthereumMethodInfo(helloReadOnlyMethod, "hello", true, false, new ArrayList<>() {{
                    add(new EthereumParameterInfo(helloReadOnlyMethod.getParameters()[0], 0,"string", null, null, null));
            }}, abi[0], null));
        }}, new ArrayList<>(), "0xSomeAddress", abiString, binary);

        assertEquals(expectedContractInfo, contractInfo);
    }



}
