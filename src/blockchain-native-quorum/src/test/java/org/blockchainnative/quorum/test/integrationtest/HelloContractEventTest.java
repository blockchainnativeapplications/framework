package org.blockchainnative.quorum.test.integrationtest;

import io.reactivex.functions.Consumer;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.ethereum.test.contracts.EthereumHelloContractWithEvent;
import org.blockchainnative.quorum.QuorumContractWrapperGenerator;
import org.blockchainnative.quorum.builder.QuorumContractInfoBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Matthias Veit
 */
public class HelloContractEventTest extends QuorumIntegrationTest {
    private static EthereumHelloContractWithEvent contract;

    @Before
    public void setup() throws IOException {
        var contractWrapperGenerator = new QuorumContractWrapperGenerator(getClientFactory(), getTransactionManagerFactory(), new TypeConverters());

        var contractInfo = new QuorumContractInfoBuilder<>(EthereumHelloContractWithEvent.class)
                .withAbi(new File(HelloContractEventTest.class.getClassLoader().getResource("contracts/compiled/HelloWorldWithEvents.abi").getFile()))
                .withBinary(new File(HelloContractEventTest.class.getClassLoader().getResource("contracts/compiled/HelloWorldWithEvents.bin").getFile()))
                .addPrivateRecipient("EgciSXJMIepHmxCSh9+j6uZWtNVoB6+btxonxgGtumg=")
                .build();

        contract = contractWrapperGenerator.generate(contractInfo);

        contract.deploy(null, null, "hello");
    }

    @Test
    public void greetingEventTest() throws InterruptedException, ExecutionException, TimeoutException {

        final var futureEvent = new CompletableFuture<EthereumHelloContractWithEvent.HelloEvent>();

        var disposable = contract.onGreeted().subscribe(helloEvent -> {
            futureEvent.complete(helloEvent);
        });

        contract.hello("Stranger");

        var event = futureEvent.get(60, TimeUnit.SECONDS);
        disposable.dispose();

        Assert.assertEquals("Stranger", event.name);
    }

    @Test
    public void greetingEventMultipleTest() throws InterruptedException, ExecutionException, TimeoutException {
        final var futureEvents = new CompletableFuture<List<EthereumHelloContractWithEvent.HelloEvent>>();
        var disposable = contract.onGreeted().subscribe(new Consumer<>() {

            private final List<EthereumHelloContractWithEvent.HelloEvent> events = new ArrayList<>();

            @Override
            public void accept(EthereumHelloContractWithEvent.HelloEvent helloEvent) {
                events.add(helloEvent);
                if (events.size() == 2) {
                    futureEvents.complete(events);
                }
            }
        });

        var person1 = "Stranger";
        var person2 = "Bill";

        contract.hello(person1);
        contract.hello(person2);

        var events = futureEvents.get(60, TimeUnit.SECONDS);
        disposable.dispose();

        Assert.assertEquals(2, events.size());
        Assert.assertEquals(person1, events.get(0).name);
        Assert.assertEquals(person2, events.get(1).name);
    }
}
