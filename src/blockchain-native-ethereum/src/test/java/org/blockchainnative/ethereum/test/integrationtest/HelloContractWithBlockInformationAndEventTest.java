package org.blockchainnative.ethereum.test.integrationtest;

import io.reactivex.functions.Consumer;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.ethereum.EthereumContractWrapperGenerator;
import org.blockchainnative.ethereum.builder.EthereumContractInfoBuilder;
import org.blockchainnative.ethereum.test.contracts.EthereumHelloContractWithBlockInformation;
import org.blockchainnative.ethereum.test.contracts.EthereumHelloContractWithEvent;
import org.blockchainnative.metadata.Event;
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
public class HelloContractWithBlockInformationAndEventTest extends EthereumIntegrationTest {
    private static EthereumHelloContractWithBlockInformation contract;

    @Before
    public void setup() throws IOException {
        var contractWrapperGenerator = new EthereumContractWrapperGenerator(getClientFactory(), getTransactionManagerFactory(), new TypeConverters());

        var contractInfo = new EthereumContractInfoBuilder<>(EthereumHelloContractWithBlockInformation.class)
                .withAbi(new File(EthereumIntegrationTest.class.getClassLoader().getResource("contracts/compiled/HelloWorldWithEvents.abi").getFile()))
                .withBinary(new File(EthereumIntegrationTest.class.getClassLoader().getResource("contracts/compiled/HelloWorldWithEvents.bin").getFile()))
                .build();

        contract = contractWrapperGenerator.generate(contractInfo);

        contract.deploy(null, null, "Hello");
    }

    @Test
    public void callHelloContractReadonly() throws ExecutionException, InterruptedException {
        var actual = contract.helloReadOnly("Test").get();

        Assert.assertEquals("Hello Test!", actual.getData());
        Assert.assertNull(actual.getBlockHash());
        Assert.assertNull(actual.getTransactionHash());
    }

    @Test
    public void callHelloContract() throws ExecutionException, InterruptedException {
        var actual = contract.hello("Test");

        Assert.assertEquals("Hello Test!", actual.getData());
        Assert.assertNotNull(actual.getBlockHash());
        Assert.assertNotNull(actual.getTransactionHash());
    }

    @Test
    public void callHelloContractAsync() throws ExecutionException, InterruptedException {
        var actual = contract.helloAsync("Test").get();

        Assert.assertEquals("Hello Test!", actual.getData());
        Assert.assertNotNull(actual.getBlockHash());
        Assert.assertNotNull(actual.getTransactionHash());
    }

    @Test
    public void greetingEventTest() throws InterruptedException, ExecutionException, TimeoutException {

        final var futureEvent = new CompletableFuture<Event<EthereumHelloContractWithEvent.HelloEvent>>();

        var disposable = contract.onGreeted().subscribe(helloEvent -> {
            futureEvent.complete(helloEvent);
        });

        contract.hello("Stranger");

        var event = futureEvent.get(60, TimeUnit.SECONDS);
        disposable.dispose();

        Assert.assertEquals("Stranger", event.getData().name);
        Assert.assertNotNull(event.getBlockHash());
        Assert.assertNotNull(event.getTransactionHash());
    }

    @Test
    public void greetingEventMultipleTest() throws InterruptedException, ExecutionException, TimeoutException {
        final var futureEvents = new CompletableFuture<List<Event<EthereumHelloContractWithEvent.HelloEvent>>>();
        var disposable = contract.onGreeted().subscribe(new Consumer<>() {

            private final List<Event<EthereumHelloContractWithEvent.HelloEvent>> events = new ArrayList<>();

            @Override
            public void accept(Event<EthereumHelloContractWithEvent.HelloEvent> helloEvent) {
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

        Assert.assertEquals(person1, events.get(0).getData().name);
        Assert.assertNotNull(events.get(0).getBlockHash());
        Assert.assertNotNull(events.get(0).getTransactionHash());

        Assert.assertEquals(person2, events.get(1).getData().name);
        Assert.assertNotNull(events.get(1).getBlockHash());
        Assert.assertNotNull(events.get(1).getTransactionHash());
    }
}
