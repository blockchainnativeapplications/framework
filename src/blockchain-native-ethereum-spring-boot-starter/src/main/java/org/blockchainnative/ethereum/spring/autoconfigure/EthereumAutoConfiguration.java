package org.blockchainnative.ethereum.spring.autoconfigure;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.blockchainnative.ContractWrapperGenerator;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.ethereum.EthereumContractWrapper;
import org.blockchainnative.ethereum.EthereumContractWrapperGenerator;
import org.blockchainnative.spring.autoconfigure.CoreAutoConfiguration;
import org.blockchainnative.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.ipc.WindowsIpcService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Matthias Veit
 */
@Configuration
@ConditionalOnClass(EthereumContractWrapper.class)
@Import(CoreAutoConfiguration.class)
@EnableConfigurationProperties(EthereumProperties.class)
public class EthereumAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumAutoConfiguration.class);

    private final EthereumProperties properties;

    @Autowired
    public EthereumAutoConfiguration(EthereumProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public Web3j web3j(){
        LOGGER.debug("Building web3j instance for endpoint address '{}", properties.getEndpointAddress());
        return new JsonRpc2_0Web3j(buildWeb3jService());
    }

    @Bean
    @ConditionalOnMissingBean
    public Supplier<Web3j> web3jFactory(@Autowired Web3j web3j) {
        LOGGER.info("Registering web3j factory");
        return () -> web3j;
    }

    @Bean
    @ConditionalOnMissingBean
    public Function<Web3j, TransactionManager> transactionManagerFactory() {
        LOGGER.info("Registering transaction manager factory");

        if (properties == null || (properties.getWallet() == null && properties.getClient() == null)) {
            var message = "Missing ethereum configuration, neither a wallet file nor an account address has been specified!";
            LOGGER.error(message);
            LOGGER.error("Use the keys '{}.wallet.path' and '{}.wallet.password' for using a wallet file, or specify '{}.client.address'.", EthereumProperties.CONTRACT_REGISTRY_PREFIX_FULL, EthereumProperties.CONTRACT_REGISTRY_PREFIX_FULL, EthereumProperties.CONTRACT_REGISTRY_PREFIX_FULL);

            throw new IllegalStateException(message);
        }

        if (properties.getWallet() != null && properties.getClient() != null) {
            LOGGER.warn("Configuration found for using both, a wallet file and a client address, defaulting to use the wallet file, if this is not the desired behaviour remove the configuration keys: '{}.wallet.path' and '{}.wallet.password'", EthereumProperties.CONTRACT_REGISTRY_PREFIX_FULL, EthereumProperties.CONTRACT_REGISTRY_PREFIX_FULL);
        }

        if (properties.getWallet() != null) {
            return getTransactionManagerFactoryFromWallet();
        } else {
            return getTransactionManagerFactoryFromClientAddress();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ContractWrapperGenerator ethereumContractWrapperGenerator(
            @Autowired Supplier<Web3j> web3jClientFactory,
            @Autowired Function<Web3j, TransactionManager> transactionManagerFactory,
            @Autowired TypeConverters typeConverters) {

        return new EthereumContractWrapperGenerator(web3jClientFactory, transactionManagerFactory, typeConverters);
    }

    private Function<Web3j, TransactionManager> getTransactionManagerFactoryFromWallet() {
        if (properties.getWallet().getPath() == null) {
            var message = "Missing Ethereum configuration, a wallet file path not specified!";
            LOGGER.error(message);

            throw new IllegalStateException(message);
        }

        if (properties.getWallet().getPassword() == null) {
            var message = "Missing Ethereum configuration, a wallet file password not specified!";
            LOGGER.error(message);

            throw new IllegalStateException(message);
        }

        var networkId = properties.getWallet().getNetworkId();
        if (networkId != null && networkId < 0) {
            LOGGER.warn("Ignoring invalid network id '{}'", networkId);
            networkId = null;
        }


        File walletFile;
        try {
            walletFile = getWalletFile(properties.getWallet().getPath());
        } catch (IOException e) {
            var message = String.format("Failed to load wallet file '%s'", properties.getWallet().getPath());
            throw new IllegalStateException(message, e);
        }

        LOGGER.info("Registering transaction manager using wallet file '{}'", walletFile.getAbsolutePath());

        if (!walletFile.exists() || !walletFile.isFile()) {
            var message = String.format("Wallet file '%s' does not exist or is no file!", walletFile.getAbsolutePath());
            LOGGER.error(message);

            throw new IllegalStateException(message);
        }

        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(properties.getWallet().getPassword(), properties.getWallet().getPath());
        } catch (IOException | CipherException e) {
            var message = String.format("Failed to read wallet file '%s'!", walletFile.getAbsolutePath());
            LOGGER.error(message);

            throw new IllegalStateException(message, e);
        }

        if (networkId != null) {
            final byte finalNetworkId = networkId;
            return (client) -> new RawTransactionManager(client, credentials, finalNetworkId);
        } else {
            return (client) -> new RawTransactionManager(client, credentials);
        }
    }

    private Web3jService buildWeb3jService(){
        var endpointAddress = properties.getEndpointAddress();

        if (StringUtil.isNullOrEmpty(endpointAddress)) {
            return new HttpService(buildHttpClient());
        } else if (endpointAddress.startsWith("http")) {
            return new HttpService(endpointAddress, buildHttpClient(), false);
        } else if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            return new WindowsIpcService(endpointAddress);
        } else {
            return new UnixIpcService(endpointAddress);
        }
    }

    private OkHttpClient buildHttpClient(){
        var builder = new OkHttpClient.Builder();

        if(properties.getHttpTimeout() != null){
            var timeout = properties.getHttpTimeout();
            builder = builder
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS);
        }
        if(LOGGER.isDebugEnabled()) {
            builder = builder.addInterceptor(
                    new HttpLoggingInterceptor(LOGGER::debug)
                            .setLevel(HttpLoggingInterceptor.Level.BODY)
            );
        }
        return builder.build();
    }

    private Function<Web3j, TransactionManager> getTransactionManagerFactoryFromClientAddress() {
        if(properties.getClient() == null || StringUtil.isNullOrEmpty(properties.getClient().getAddress())){
            var message = "Missing ethereum configuration, client address not specified";
            LOGGER.error(message);

            throw new IllegalStateException(message);
        }

        LOGGER.info("Registering transaction manager using client address '{}'", properties.getClient().getAddress());

        return (client) -> new ClientTransactionManager(client, properties.getClient().getAddress());
    }

    private static File getWalletFile(String fileName) throws IOException {
        if (fileName.startsWith("classpath:")) {
            return new ClassPathResource(fileName.replace("classpath:", "")).getFile();
        } else {
            return new File(fileName);
        }
    }
}
