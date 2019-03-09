package org.blockchainnative.fabric.test.integrationtest;

import org.blockchainnative.fabric.FabricContractWrapperGenerator;
import org.blockchainnative.fabric.builder.FabricContractInfoBuilder;
import org.blockchainnative.fabric.metadata.ChaincodeLanguage;
import org.blockchainnative.fabric.metadata.FabricContractInfo;
import org.blockchainnative.fabric.test.contracts.FabricHelloContractWithEventAndResult;
import org.blockchainnative.metadata.Event;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;


/**
 * @author Matthias Veit
 */
public class HelloContractWithEventAndResultIT extends FabricIntegrationTest {
    private static FabricContractInfo<FabricHelloContractWithEventAndResult> contractInfo;
    private static FabricHelloContractWithEventAndResult contract;

    @BeforeClass
    public static void setup() throws IOException, ChaincodeEndorsementPolicyParseException {
        var policy = new ChaincodeEndorsementPolicy();
        policy.fromYamlFile(new File(FabricIntegrationTest.class.getClassLoader().getResource("FooAndBarPolicy.yaml").getFile()));


        var identifier = UUID.randomUUID().toString().replace("-", "");

        contractInfo = new FabricContractInfoBuilder<>(FabricHelloContractWithEventAndResult.class)
                .withIdentifier(identifier)
                // use random name and version so we don't need to reset the blockchain all the time
                .withChainCodeIdentifier(ChaincodeID.newBuilder().setName(identifier).setVersion(identifier).setPath("hello").build())
                .withChainCodeLanguage(ChaincodeLanguage.Go)
                .withChaincodeSourceDirectory(FabricIntegrationTest.class.getClassLoader().getResource("chaincode/hello").getFile())
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

        contract.instantiate("Hi", peers, getFooAdmin());
    }

    @Test
    public void callHello() {
        var result = contract.hello("Test");

        assertEquals("Hi Test!", result.getData());
        assertNotNull(result.getBlockHash());
        assertNotNull(result.getTransactionHash());
    }

    @Test
    public void callHelloAsync() throws ExecutionException, InterruptedException {
        var future = contract.helloAsync("Test");
        var result = future.get();

        assertEquals("Hi Test!", result.getData());
        assertNotNull(result.getBlockHash());
        assertNotNull(result.getTransactionHash());
    }

    @Test
    public void callHelloReadOnly() throws ExecutionException, InterruptedException {
        var future = contract.helloReadOnly("Test");
        var result = future.get();

        assertEquals("Hi Test!", result.getData());
        assertNull(result.getBlockHash());
        assertNull(result.getTransactionHash());
    }

    @Test
    public void callHelloAndWaitForEvent() throws ExecutionException, InterruptedException {
        var future = new CompletableFuture<Event<FabricHelloContractWithEventAndResult.HelloEvent>>();
        var disposable = contract.onGreeted()
                .subscribe(helloEvent -> future.complete(helloEvent));

        contract.hello("Foo");
        var event = future.get();

        disposable.dispose();
        assertEquals("Foo", event.getData().name);
        assertNotNull(event.getBlockHash());
        assertNotNull(event.getTransactionHash());
    }
}
