package org.blockchainnative.quorum.test.integrationtest;

import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.ethereum.test.contracts.EthereumHelloContract;
import org.blockchainnative.quorum.QuorumContractWrapperGenerator;
import org.blockchainnative.quorum.builder.QuorumContractInfoBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author Matthias Veit
 */
public class HelloContractIT extends QuorumIntegrationTest {
    private static EthereumHelloContract contract;

    @BeforeClass
    public static void setup() throws IOException {

        var contractWrapperGenerator = new QuorumContractWrapperGenerator(getClientFactory(), getTransactionManagerFactory(), new TypeConverters());
        var contractInfo = new QuorumContractInfoBuilder<>(EthereumHelloContract.class)
                .atAddress(null)
                .withAbi(new File(HelloContractIT.class.getClassLoader().getResource("contracts/compiled/HelloWorld.abi").getFile()))
                .withBinary(new File(HelloContractIT.class.getClassLoader().getResource("contracts/compiled/HelloWorld.bin").getFile()))
                .build();

        contract = contractWrapperGenerator.generate(contractInfo);

        contract.deploy(null, null, "Hello");
    }


    @Test
    public void callHelloContractReadonly() throws ExecutionException, InterruptedException {
        var actual = contract.helloReadOnly("Test").get();

        Assert.assertEquals("Hello Test!", actual);
    }

    @Test
    public void callHelloContract(){
        var actual = contract.hello("Test");

        Assert.assertEquals("Hello Test!", actual);
    }
}
