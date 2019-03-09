package org.blockchainnative.quorum.test.integrationtest;

import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.quorum.QuorumContractWrapperGenerator;
import org.blockchainnative.quorum.builder.QuorumContractInfoBuilder;
import org.blockchainnative.quorum.test.contracts.SimpleStorageContract;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Matthias Veit
 */
public class SimpleStoragePrivateContractIT extends QuorumIntegrationTest {
    private static SimpleStorageContract contract;

    @BeforeClass
    public static void setup() throws IOException {

        var contractWrapperGenerator = new QuorumContractWrapperGenerator(getClientFactory(), getTransactionManagerFactory(), new TypeConverters());
        var contractInfo = new QuorumContractInfoBuilder<>(SimpleStorageContract.class)
                .atAddress(null)
                .withAbi(new File(SimpleStoragePrivateContractIT.class.getClassLoader().getResource("contracts/compiled/SimpleStorage.abi").getFile()))
                .withBinary(new File(SimpleStoragePrivateContractIT.class.getClassLoader().getResource("contracts/compiled/SimpleStorage.bin").getFile()))
                .addPrivateRecipient("EgciSXJMIepHmxCSh9+j6uZWtNVoB6+btxonxgGtumg=")
                .build();

        contract = contractWrapperGenerator.generate(contractInfo);

        contract.deploy();
    }


    @Test
    public void callContract() {
        var initial = contract.get();

        assertThat(initial, is(0));

        contract.set(42, null);

        var modified = contract.get();

        assertThat(modified, is(42));
    }
}
