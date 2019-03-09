package org.blockchainnative.ethereum.test.integrationtest;

import org.blockchainnative.ethereum.transactions.Ethereum;
import org.blockchainnative.ethereum.transactions.EthereumBlock;
import org.blockchainnative.ethereum.transactions.EthereumTransaction;
import org.blockchainnative.ethereum.transactions.EthereumTransactionBuilder;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;


/**
 * @author Matthias Veit
 */
public class TransactionTest extends EthereumIntegrationTest {

    @Test
    public void testSimpleTransaction() {
        var web3j = getClientFactory().get();
        var transactionManager = getTransactionManagerFactory().apply(web3j);

        var transaction = new EthereumTransactionBuilder(web3j, transactionManager)
                .withRecipient("0x0000000000000000000000000000000000000001")
                .withData("Hello World".getBytes(StandardCharsets.UTF_8))
                .build();

        var hash = transaction.send();

        assertThat(hash, notNullValue());

        var ethereum = new Ethereum(web3j);

        var transactionData = ethereum.getTransactionByHash(hash);

        assertThat(transactionData, is(notNullValue()));
        assertThat(transactionData.getData(), is("Hello World".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void blockObservableTest() throws InterruptedException, ExecutionException, TimeoutException {
        var ethereum = new Ethereum(getClientFactory().get());

        var blockFuture = new CompletableFuture<EthereumBlock>();
        ethereum.getBlockObservable()
                .take(1)
                .subscribe(block -> blockFuture.complete(block));

        var client = getClientFactory().get();
        var transactionManager = getTransactionManagerFactory().apply(client);

        var transactionRequest = new EthereumTransactionBuilder(client, transactionManager)
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
        var quorum = new Ethereum(getClientFactory().get());

        var transactionFuture = new CompletableFuture<EthereumTransaction>();
        quorum.getTransactionObservable()
                .take(1)
                .subscribe(transaction -> transactionFuture.complete(transaction));

        var client = getClientFactory().get();
        var transactionManager = getTransactionManagerFactory().apply(client);

        var transactionRequest = new EthereumTransactionBuilder(client, transactionManager)
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
