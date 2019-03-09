package org.blockchainnative.ethereum.test.integrationtest;

import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.ethereum.EthereumContractWrapperGenerator;
import org.blockchainnative.ethereum.ConfigurablePollingTransactionReceiptProcessor;
import org.blockchainnative.ethereum.builder.EthereumContractInfoBuilder;
import org.blockchainnative.ethereum.test.contracts.EthereumHelloContractWithBlockInformation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

/**
 * @author Matthias Veit
 */
public class ConfirmationBlocksIT extends EthereumIntegrationTest {
    private static EthereumHelloContractWithBlockInformation contract;

    protected static Function<Web3j, TransactionManager> getTransactionManagerFactory() {
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials("Start123!", new File(EthereumIntegrationTest.class.getClassLoader().getResource("wallet.json").getFile()));
        } catch (IOException | CipherException e) {
            throw new RuntimeException(e);
        }

        return (client) -> new RawTransactionManager(client, credentials, (byte) 42, new ConfigurablePollingTransactionReceiptProcessor(client, 4));
    }

    @BeforeClass
    public static void setup() throws IOException {
        var contractWrapperGenerator = new EthereumContractWrapperGenerator(getClientFactory(), getTransactionManagerFactory(), new TypeConverters());

        var contractInfo = new EthereumContractInfoBuilder<>(EthereumHelloContractWithBlockInformation.class)
                .withAbi(new File(EthereumIntegrationTest.class.getClassLoader().getResource("contracts/compiled/HelloWorldWithEvents.abi").getFile()))
                .withBinary(new File(EthereumIntegrationTest.class.getClassLoader().getResource("contracts/compiled/HelloWorldWithEvents.bin").getFile()))
                .build();

        contract = contractWrapperGenerator.generate(contractInfo);

        contract.deploy(null, null, "Hello");
    }


    @Test
    public void testConfirmationBlocksTransactionProcessor() throws IOException {
        var web3j = getClientFactory().get();

        var currentBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
        var result = contract.hello("Foo");

        var resultBlockNumber = web3j.ethGetBlockByHash(result.getBlockHash(), false).send().getBlock().getNumber();

        Assert.assertTrue(resultBlockNumber.subtract(currentBlockNumber).signum() > -1);
    }
}
