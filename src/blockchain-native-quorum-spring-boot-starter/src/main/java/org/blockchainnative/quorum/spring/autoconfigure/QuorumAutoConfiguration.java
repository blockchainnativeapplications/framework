package org.blockchainnative.quorum.spring.autoconfigure;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.blockchainnative.ContractWrapperGenerator;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.quorum.QuorumContractWrapper;
import org.blockchainnative.quorum.QuorumContractWrapperGenerator;
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
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.ipc.WindowsIpcService;
import org.web3j.quorum.JsonRpc2_0Quorum;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
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
@ConditionalOnClass(QuorumContractWrapper.class)
@Import(CoreAutoConfiguration.class)
@EnableConfigurationProperties(QuorumProperties.class)
public class QuorumAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuorumAutoConfiguration.class);

    private final QuorumProperties properties;

    @Autowired
    public QuorumAutoConfiguration(QuorumProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public Quorum quorum(){
        if(properties == null || StringUtil.isNullOrEmpty(properties.getEndpointAddress())){
            var message = "Missing quorum configuration, endpoint address not specified";
            LOGGER.error(message);

            throw new IllegalStateException(message);
        }
        LOGGER.debug("Building quorum instance for endpoint address '{}", properties.getEndpointAddress());
        return new JsonRpc2_0Quorum(buildWeb3jService());
    }

    @Bean
    @ConditionalOnMissingBean
    public Supplier<Quorum> web3jFactory(@Autowired Quorum quorum) {
        LOGGER.info("Registering quorum factory");
        return () -> quorum;
    }

    @Bean
    @ConditionalOnMissingBean
    public Function<Quorum, TransactionManager> transactionManagerFactory() {
        LOGGER.info("Registering transaction manager factory");

        if (properties == null || properties.getClient() == null) {
            var message = "Missing quorum configuration, no account address has been specified!";
            LOGGER.error(message);
            LOGGER.error("Use the keys '{}.endpoint-address' and '{}.client.address' to specify the URL to connect to quorum and the account address used for transactions.", QuorumProperties.CONTRACT_REGISTRY_PREFIX_FULL, QuorumProperties.CONTRACT_REGISTRY_PREFIX_FULL);

            throw new IllegalStateException(message);
        }

        return getTransactionManagerFactoryFromClientAddress();
    }

    @Bean
    @ConditionalOnMissingBean
    public ContractWrapperGenerator ethereumContractWrapperGenerator(
            @Autowired Supplier<Quorum> quorumClientFactory,
            @Autowired Function<Quorum, TransactionManager> transactionManagerFactory,
            @Autowired TypeConverters typeConverters) {

        return new QuorumContractWrapperGenerator(quorumClientFactory, transactionManagerFactory, typeConverters);
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

    private Function<Quorum, TransactionManager> getTransactionManagerFactoryFromClientAddress() {
        if(properties.getClient() == null || StringUtil.isNullOrEmpty(properties.getClient().getAddress())){
            var message = "Missing quorum configuration, client address not specified";
            LOGGER.error(message);

            throw new IllegalStateException(message);
        }

        LOGGER.info("Registering transaction manager using client address '{}'", properties.getClient().getAddress());

        final var clientAddress = properties.getClient().getAddress();
        final var privateFrom = StringUtil.isNullOrEmpty(properties.getClient().getPrivateFrom()) ? null : properties.getClient().getPrivateFrom();
        final var privateFor = properties.getClient().getPrivateFor() != null && properties.getClient().getPrivateFor().isEmpty() ? null : properties.getClient().getPrivateFor();

        return (client) -> new ClientTransactionManager(client, clientAddress, privateFrom, privateFor);
    }

    private static File getWalletFile(String fileName) throws IOException {
        if (fileName.startsWith("classpath:")) {
            return new ClassPathResource(fileName.replace("classpath:", "")).getFile();
        } else {
            return new File(fileName);
        }
    }
}
