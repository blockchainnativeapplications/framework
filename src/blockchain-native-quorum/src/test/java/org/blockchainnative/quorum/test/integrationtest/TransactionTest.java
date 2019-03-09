package org.blockchainnative.quorum.test.integrationtest;

import org.blockchainnative.quorum.transactions.Quorum;
import org.blockchainnative.quorum.transactions.QuorumBlock;
import org.blockchainnative.quorum.transactions.QuorumTransaction;
import org.blockchainnative.quorum.transactions.QuorumTransactionBuilder;
import org.blockchainnative.transactions.exceptions.BlockchainException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

/**
 * @author Matthias Veit
 */
public class TransactionTest extends QuorumIntegrationTest {

    @BeforeClass
    public static void beforeClass() throws IOException {
        var admin1 = Admin.build(new HttpService(getQuorumUrl()));

        admin1.personalUnlockAccount(getSenderAddress(), "", BigInteger.ZERO).send();
    }

    @Test
    public void testPublicTransaction() {
        var client = getClientFactory().get();
        var transactionManager = getTransactionManagerFactory().apply(client);

        var data = "Hello Quorum".getBytes(StandardCharsets.UTF_8);

        var quorum = new Quorum(client);
        var transactionRequest = new QuorumTransactionBuilder(client, transactionManager)
                .withNullRecipient()
                .withData(data)
                .build();

        var transactionHash = transactionRequest.send();

        var transaction = quorum.getTransactionByHash(transactionHash);

        assertEquals(new String(transaction.getData(), StandardCharsets.UTF_8), new String(data, StandardCharsets.UTF_8));
        assertThat(transaction.isPrivate(), is(false));
    }

    @Test
    public void testPrivateTransaction() {
        var client = getClientFactory().get();
        var transactionManager = getTransactionManagerFactory().apply(client);

        var data = "Hello Quorum".getBytes(StandardCharsets.UTF_8);

        var quorum = new Quorum(client);
        var transactionRequest = new QuorumTransactionBuilder(client, transactionManager)
                .withNullRecipient()
                .addPrivateRecipient("EgciSXJMIepHmxCSh9+j6uZWtNVoB6+btxonxgGtumg=")
                .withData(data)
                .build();

        var transactionHash = transactionRequest.send();

        var transaction = quorum.getTransactionByHash(transactionHash);


        assertEquals(new String(transaction.getData(), StandardCharsets.UTF_8), new String(data, StandardCharsets.UTF_8));
        assertThat(transaction.isPrivate(), is(true));
    }

    @Test(expected = BlockchainException.class)
    public void getNonExistingBlockByHash() {
        var quorum = new Quorum(getClientFactory().get());
        var block = quorum.getBlockByHash("0x0000000000000000000000000000000000000000000000000000000000000000");

        fail();
    }

    @Test(expected = BlockchainException.class)
    public void getNegativeBlockByNumber() {
        var quorum = new Quorum(getClientFactory().get());
        var block = quorum.getBlockByNumber(BigInteger.valueOf(-1));

        fail();
    }

    @Test(expected = BlockchainException.class)
    public void getNonExistingBlockByNumber() {
        var quorum = new Quorum(getClientFactory().get());
        var block = quorum.getBlockByNumber(BigInteger.valueOf(Integer.MAX_VALUE));

        fail();
    }

    @Test(expected = BlockchainException.class)
    public void getNonExistingTransaction() {
        var quorum = new Quorum(getClientFactory().get());
        var transaction = quorum.getTransactionByHash("0x0000000000000000000000000000000000000000000000000000000000000000");

        fail();
    }

    @Test
    public void blockObservableTest() throws InterruptedException, ExecutionException, TimeoutException {
        var quorum = new Quorum(getClientFactory().get());

        var blockFuture = new CompletableFuture<QuorumBlock>();
        quorum.getBlockObservable()
                .take(1)
                .subscribe(block -> blockFuture.complete(block));

        var client = getClientFactory().get();
        var transactionManager = getTransactionManagerFactory().apply(client);

        var transactionRequest = new QuorumTransactionBuilder(client, transactionManager)
                .withNullRecipient()
                .build();

        transactionRequest.send();

        var block = blockFuture.get(60, TimeUnit.SECONDS);

        assertThat(block, notNullValue());
        assertThat(block.getBlockHash(), notNullValue());
        assertThat(block.getBlockNumber(), notNullValue());
        assertThat(block.getTimestamp(), notNullValue());
    }

    @Test
    public void transactionObservableTest() throws InterruptedException, ExecutionException, TimeoutException {
        var quorum = new Quorum(getClientFactory().get());

        var transactionFuture = new CompletableFuture<QuorumTransaction>();
        quorum.getTransactionObservable()
                .take(1)
                .subscribe(transaction -> transactionFuture.complete(transaction));

        var client = getClientFactory().get();
        var transactionManager = getTransactionManagerFactory().apply(client);

        var transactionRequest = new QuorumTransactionBuilder(client, transactionManager)
                .withNullRecipient()
                .build();

        transactionRequest.send();

        var transaction = transactionFuture.get(60, TimeUnit.SECONDS);

        assertThat(transaction, notNullValue());
        assertThat(transaction.getBlockHash(), notNullValue());
        assertThat(transaction.getBlockNumber(), notNullValue());
        assertThat(transaction.getSender(), notNullValue());
        assertThat(transaction.getRecipient(), is(transactionRequest.getRecipient()));
    }
}
