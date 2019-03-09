package org.blockchainnative.fabric.test.integrationtest;

import org.blockchainnative.fabric.FabricContractWrapperGenerator;
import org.blockchainnative.fabric.builder.FabricContractInfoBuilder;
import org.blockchainnative.fabric.metadata.ChaincodeLanguage;
import org.blockchainnative.fabric.metadata.FabricContractInfo;
import org.blockchainnative.fabric.test.contracts.AddContract;
import org.blockchainnative.fabric.test.contracts.FabricHelloContract;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthias Veit
 */
public class AddContractIT extends FabricIntegrationTest {
    private static FabricContractInfo<AddContract> contractInfo;
    private static AddContract contract;

    @BeforeClass
    public static void installAdd() throws IOException, ChaincodeEndorsementPolicyParseException, InvalidArgumentException, ProposalException {
        var policy = new ChaincodeEndorsementPolicy();
        policy.fromYamlFile(new File(FabricIntegrationTest.class.getClassLoader().getResource("FooAndBarPolicy.yaml").getFile()));

        var identifier = UUID.randomUUID().toString().replace("-", "");

        contractInfo = new FabricContractInfoBuilder<>(AddContract.class)
                .withIdentifier(identifier)
                // use random name and version so we don't need to reset the blockchain all the time
                .withChainCodeIdentifier(ChaincodeID.newBuilder().setName(identifier).setVersion(identifier).setPath("add").build())
                .withChainCodeLanguage(ChaincodeLanguage.Go)
                .withChaincodeSourceDirectory(FabricIntegrationTest.class.getClassLoader().getResource("chaincode/add").getFile())
                .withChaincodePolicy(policy)
                .build();

        var wrapperGenerator = new FabricContractWrapperGenerator(getClientFactory(), getChannelFactory(), getTypeConverters());
        contract = wrapperGenerator.generate(contractInfo);

        var peers = new HashSet<String>() {{
            add("peer0.foo.bcn.org");
            add("peer0.bar.bcn.org");
        }};

        contract.install(new HashSet<>() {{
            add("peer0.foo.bcn.org");
        }}, getFooAdmin());
        contract.install(new HashSet<>() {{
            add("peer0.bar.bcn.org");
        }}, getBarAdmin());

        contract.instantiate(peers, getFooAdmin());
    }

    @Test
    public void testIntToStringConversion() {
        var result = contract.addInt(3, 4);

        assertEquals(7, result);
    }
}
