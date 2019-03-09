package org.blockchainnative.fabric.test;

import org.blockchainnative.annotations.ContractEvent;
import org.blockchainnative.annotations.SmartContract;
import org.blockchainnative.fabric.Constants;
import org.blockchainnative.fabric.builder.FabricContractInfoBuilder;
import org.blockchainnative.fabric.metadata.ChaincodeLanguage;
import org.blockchainnative.fabric.metadata.FabricContractInfo;
import org.blockchainnative.fabric.test.contracts.FabricHelloContract;
import org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations;
import org.blockchainnative.fabric.test.integrationtest.FabricIntegrationTest;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * @author Matthias Veit
 */
public class ContractBuilderTest {

    @Test
    public void buildHelloWorldContractInfoWithoutAnnotations() throws IOException, NoSuchMethodException, ChaincodeEndorsementPolicyParseException {
        var policy = new ChaincodeEndorsementPolicy();
        policy.fromYamlFile(new File(FabricIntegrationTest.class.getClassLoader().getResource("FooAndBarPolicy.yaml").getFile()));

        var identifier = "id";
        var chaincodeId = ChaincodeID.newBuilder().setName("hello").setVersion("1.0").setPath("hello").build();
        var targetPeers = new HashSet<String>() {{
            add("peer0.foo.bcn.org");
            add("peer0.bar.bcn.org");
        }};

        var contractInfo = getHelloContractWithoutAnnotationsContractInfo(identifier, chaincodeId, policy, targetPeers);

        assertEquals(identifier, contractInfo.getIdentifier());
        assertEquals(chaincodeId, contractInfo.getChaincodeID());
        assertEquals(policy, contractInfo.getChaincodePolicy());
        assertNotNull(contractInfo.getChaincodeSourceDirectory());
        assertEquals(targetPeers, contractInfo.getTargetPeerNames());
        assertEquals(5, contractInfo.getMethodInfos().size());

        var methodInfo = contractInfo.getMethodInfo(FabricHelloContractWithoutAnnotations.class.getMethod("helloReadOnly", String.class));
        assertEquals("hello", methodInfo.getContractMethodName());
        assertFalse(methodInfo.getResultTypeConverterClass().isPresent());
        assertEquals(1, methodInfo.getParameterInfos().size());
        assertEquals(Optional.of(String.class), methodInfo.getParameterInfos().get(0).getPassParameterAsType());
        assertEquals(null, methodInfo.getParameterInfos().get(0).getSpecialArgumentName());
        assertEquals(1, contractInfo.getEventInfos().size());

        var eventInfo = contractInfo.getEventInfo("greeted");
        assertEquals(0, eventInfo.getEventParameterInfos().size());
        assertEquals(1, eventInfo.getEventFieldInfos().size());
        assertEquals(Optional.empty(), eventInfo.getEventFieldInfos().get(0).getSourceFieldIndex());
    }

    @Test(expected = IllegalStateException.class)
    public void emptyChaincodeIdentifierShouldFail() {
        var contractInfo = new FabricContractInfoBuilder<>(FabricHelloContract.class)
                .build();

        fail("Should fail because of missing chaincode identifier");
    }

    @Test(expected = IllegalStateException.class)
    public void illegalContractEventShouldFail() {
        var contractInfo = new FabricContractInfoBuilder<>(IllegalEventContract.class)
                .withChainCodeIdentifier(ChaincodeID.newBuilder().setName("test").build())
                .build();

        fail("Should fail because of illegal event type");
    }

    public static FabricContractInfo<FabricHelloContractWithoutAnnotations> getHelloContractWithoutAnnotationsContractInfo(String identifier, ChaincodeID chaincodeId) throws IOException, ChaincodeEndorsementPolicyParseException {
        var policy = new ChaincodeEndorsementPolicy();
        policy.fromYamlFile(new File(FabricIntegrationTest.class.getClassLoader().getResource("FooAndBarPolicy.yaml").getFile()));

        var targetPeers = new HashSet<String>() {{
            add("peer0.foo.bcn.org");
            add("peer0.bar.bcn.org");
        }};

        return getHelloContractWithoutAnnotationsContractInfo(identifier, chaincodeId, policy, targetPeers);
    }

    public static FabricContractInfo<FabricHelloContractWithoutAnnotations> getHelloContractWithoutAnnotationsContractInfo(String identifier, ChaincodeID chaincodeId, ChaincodeEndorsementPolicy policy, Set<String> targetPeers) {
        return new FabricContractInfoBuilder<>(FabricHelloContractWithoutAnnotations.class)
                .withIdentifier(identifier)
                .withChainCodeIdentifier(chaincodeId)
                .withChaincodePolicy(policy)
                .withChaincodeSourceDirectory("chaincode/hello")
                .withChainCodeLanguage(ChaincodeLanguage.Go)
                .withTargetPeers(targetPeers)
                .method("hello", String.class)
                    .name("hello")
                    .parameter(0)
                        .build()
                    .build()
                .method("helloAsync", String.class)
                    .name("hello")
                    .parameter(0)
                        .build()
                    .build()
                .method("helloReadOnly", String.class)
                    .name("hello")
                    .readonly(true)
                    .parameter(0)
                        .passParameterAsType(String.class)
                        .build()
                    .build()
                .method("install", Set.class, User.class)
                    .specialMethod(true)
                    .parameter(0)
                        .passAsSpecialArgWithName(Constants.TARGET_PEERS_ARGUMENT)
                        .build()
                    .parameter(1)
                        .passAsSpecialArgWithName(Constants.USER_ARGUMENT)
                        .build()
                    .build()
                .method("instantiate", String.class, Set.class, User.class)
                    .specialMethod(true)
                    .parameter(1)
                        .passAsSpecialArgWithName(Constants.TARGET_PEERS_ARGUMENT)
                        .build()
                    .parameter(2)
                        .passAsSpecialArgWithName(Constants.USER_ARGUMENT)
                        .build()
                    .build()
                .event("onGreeted")
                    .name("greeted")
                    .eventField("name")
                        .sourceFieldName("name")
                        .build()
                    .build()
                .build();
    }

    @SmartContract
    interface IllegalEventContract {

        @ContractEvent
        void event();
    }
}
