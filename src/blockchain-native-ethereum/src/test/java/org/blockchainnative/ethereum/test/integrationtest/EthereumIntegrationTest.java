package org.blockchainnative.ethereum.test.integrationtest;

import org.blockchainnative.test.IntegrationTest;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Matthias Veit
 */
@Category(IntegrationTest.class)
public class EthereumIntegrationTest {
    protected static final String TEST_PROPERTIES_FILE_NAME = "test.properties";
    protected static Properties testProperties;

    @BeforeClass
    public static void loadProperties() throws IOException {
        InputStream propertiesStream = null;
        try {
             propertiesStream = EthereumIntegrationTest.class.getClassLoader().getResourceAsStream(TEST_PROPERTIES_FILE_NAME);
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

    protected static String getEthereumUrl(){
        var defaultValue = "http://localhost:8545";
        if(testProperties != null){
            return testProperties.getProperty("url", defaultValue);
        }
        return defaultValue;
    }

    protected static Supplier<Web3j> getClientFactory(){
        return () -> new JsonRpc2_0Web3j(new HttpService(getEthereumUrl()));
    }

    protected static Function<Web3j, TransactionManager> getTransactionManagerFactory(){
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials("Start123!", new File(EthereumIntegrationTest.class.getClassLoader().getResource("wallet.json").getFile()));
        } catch (IOException | CipherException e) {
            throw new RuntimeException(e);
        }

        return (client) -> new RawTransactionManager(client, credentials, (byte)42);
    }
}
