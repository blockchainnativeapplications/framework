package org.blockchainnative.fabric.test.integrationtest;

import org.blockchainnative.fabric.FabricContractWrapperGenerator;
import org.blockchainnative.fabric.metadata.FabricContractInfo;
import org.blockchainnative.fabric.test.ContractBuilderTest;
import org.blockchainnative.fabric.test.contracts.FabricHelloContractWithoutAnnotations;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthias Veit
 */
public class HelloContractWithoutAnnotationsIT extends FabricIntegrationTest {
    private static FabricContractInfo<FabricHelloContractWithoutAnnotations> contractInfo;
    private static FabricHelloContractWithoutAnnotations contract;

    @BeforeClass
    public static void setup() throws IOException, ChaincodeEndorsementPolicyParseException {

        var identifier = UUID.randomUUID().toString().replace("-", "");

        contractInfo = ContractBuilderTest.getHelloContractWithoutAnnotationsContractInfo(identifier, ChaincodeID.newBuilder().setName(identifier).setVersion(identifier).setPath("hello").build());

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

        assertEquals("Hi Test!", result);
    }

    @Test
    public void callHelloAsync() throws ExecutionException, InterruptedException {
        var future = contract.helloAsync("Test");
        var result = future.get();

        assertEquals("Hi Test!", result);
    }

    @Test
    public void callHelloReadOnly() throws ExecutionException, InterruptedException {
        var future = contract.helloReadOnly("Test");
        var result = future.get();

        assertEquals("Hi Test!", result);
    }

    @Test
    public void callHelloAndWaitForEvent() throws ExecutionException, InterruptedException {
        var future = new CompletableFuture<FabricHelloContractWithoutAnnotations.HelloEvent>();
        var disposable = contract.onGreeted()
                .subscribe(helloEvent -> future.complete(helloEvent));

        contract.hello("Foo");
        var event = future.get();

        disposable.dispose();
        assertEquals("Foo", event.name);
    }
}
