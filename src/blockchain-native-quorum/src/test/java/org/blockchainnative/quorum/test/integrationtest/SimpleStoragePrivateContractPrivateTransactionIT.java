package org.blockchainnative.quorum.test.integrationtest;

import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.quorum.QuorumContractWrapperGenerator;
import org.blockchainnative.quorum.builder.QuorumContractInfoBuilder;
import org.blockchainnative.quorum.test.contracts.SimpleStorageContract;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.protocol.http.HttpService;
import org.web3j.quorum.JsonRpc2_0Quorum;
import org.web3j.quorum.Quorum;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Matthias Veit
 */
public class SimpleStoragePrivateContractPrivateTransactionIT extends QuorumIntegrationTest {
    private static SimpleStorageContract contract;

    @BeforeClass
    public static void setup() throws IOException {

        var contractWrapperGenerator = new QuorumContractWrapperGenerator(getClientFactory(), getTransactionManagerFactory(), new TypeConverters());
        var contractInfo = new QuorumContractInfoBuilder<>(SimpleStorageContract.class)
                .atAddress(null)
                .withAbi(new File(SimpleStoragePrivateContractPrivateTransactionIT.class.getClassLoader().getResource("contracts/compiled/SimpleStorage.abi").getFile()))
                .withBinary(new File(SimpleStoragePrivateContractPrivateTransactionIT.class.getClassLoader().getResource("contracts/compiled/SimpleStorage.bin").getFile()))
                .addPrivateRecipient("EgciSXJMIepHmxCSh9+j6uZWtNVoB6+btxonxgGtumg=")
                .addPrivateRecipient("8YfHSZXK4ss/71vwRL5tFndzOTMpHpwTqfjJJ0cPmXk=")
                .build();

        contract = contractWrapperGenerator.generate(contractInfo);

        contract.deploy();
    }


    @Test
    public void callContract() {
        var initial = contract.get();

        assertThat(initial, is(0));

        contract.set(43, null);
        var modified = contract.get();

        assertThat(modified, is(43));

        contract.set(42, new ArrayList<>() {{
            add("EgciSXJMIepHmxCSh9+j6uZWtNVoB6+btxonxgGtumg=");
        }});

        var modifiedFromNode1 = contract.get();

        assertThat(modifiedFromNode1, is(42));

        Supplier<Quorum> x = () -> new JsonRpc2_0Quorum(new HttpService(getQuorumUrl().replace(":22000", ":24000")));
        var contractFromNode3 = new QuorumContractWrapperGenerator(x, getTransactionManagerFactory(), new TypeConverters()).generate(contract.getContractInfo());

        var modifiedFromNode3 = contractFromNode3.get();

        assertThat(modifiedFromNode3, is(43));
    }
}
