package org.blockchainnative.quorum.test.integrationtest;

import org.blockchainnative.ethereum.transactions.EthereumBaseTransactionRequest;
import org.blockchainnative.test.IntegrationTest;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.web3j.protocol.http.HttpService;
import org.web3j.quorum.JsonRpc2_0Quorum;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Matthias Veit
 */
@Category(IntegrationTest.class)
public class QuorumIntegrationTest {
    protected static final String TEST_PROPERTIES_FILE_NAME = "test.properties";
    protected static Properties testProperties;

    @BeforeClass
    public static void loadProperties() throws IOException {
        InputStream propertiesStream = null;
        try {
             propertiesStream = QuorumIntegrationTest.class.getClassLoader().getResourceAsStream(TEST_PROPERTIES_FILE_NAME);
            if (propertiesStream != null) {
                testProperties = new Properties();
                testProperties.load(propertiesStream);
            }
        }finally {
            if(propertiesStream != null) {
                propertiesStream.close();
            }
        }
    }

    protected static String getProperty(String key, String defaultValue){
        if(testProperties != null && testProperties.containsKey(key)){
            return testProperties.getProperty(key, defaultValue);
        }
        return defaultValue;
    }

    protected static String getQuorumUrl(){
        return getProperty("url", "http://localhost:22000");
    }

    protected static String getSenderAddress(){
        return getProperty("sender-address", EthereumBaseTransactionRequest.NULL_RECIPIENT_ADDRESS);
    }

    protected static String getPrivateFrom(){
        return getProperty("private-from", null);
    }

    protected static List<String> getPrivateFor(){
        var value = getProperty("private-for", null);
        if(value != null){
            return Arrays.stream(value.split(","))
                    .collect(Collectors.toList());
        }
        return null;
    }

    protected static Supplier<Quorum> getClientFactory(){
        return () -> new JsonRpc2_0Quorum(new HttpService(getQuorumUrl()));
    }

    protected static Function<Quorum, TransactionManager> getTransactionManagerFactory(){
        final String from = getSenderAddress();
        return (client) -> new ClientTransactionManager(client, from, getPrivateFrom(), getPrivateFor());
    }
}
