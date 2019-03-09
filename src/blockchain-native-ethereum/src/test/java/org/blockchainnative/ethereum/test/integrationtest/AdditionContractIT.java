package org.blockchainnative.ethereum.test.integrationtest;

import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.ethereum.builder.EthereumContractInfoBuilder;
import org.blockchainnative.ethereum.EthereumContractWrapperGenerator;
import org.blockchainnative.ethereum.test.contracts.EthereumAdditionContract;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Matthias Veit
 */
public class AdditionContractIT extends EthereumIntegrationTest {
    private static EthereumAdditionContract contract;

    @BeforeClass
    public static void setup() throws IOException {
        var contractWrapperGenerator = new EthereumContractWrapperGenerator(getClientFactory(), getTransactionManagerFactory(), new TypeConverters());
        var contractInfo = new EthereumContractInfoBuilder<>(EthereumAdditionContract.class)
                .atAddress(null)
                .withAbi(new File(AdditionContractIT.class.getClassLoader().getResource("contracts/compiled/Addition.abi").getFile()))
                .withBinary(new File(AdditionContractIT.class.getClassLoader().getResource("contracts/compiled/Addition.bin").getFile()))
                .method("add", Integer.TYPE, Integer.TYPE)
                    .readonly(true)
                    .build()
                .build();

        contract = contractWrapperGenerator.generate(contractInfo);

        contract.deploy();
    }

    @Test
    public void callAdditionContractReadonly() {
        var actual = contract.add(3, 4);

        Assert.assertEquals(7, actual);
    }

    @Test
    public void callAdditionInTransaction() {
        var actual = contract.add(3, 4);

        Assert.assertEquals(7, actual);
    }

    @Test
    public void callAdditionContractIgnoreResult() {
        try {
            contract.addButIgnoreResult(3, 4);
        }catch (Throwable e){
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
