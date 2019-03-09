package org.blockchainnative.fabric.spring.autoconfigure;

import org.blockchainnative.ContractWrapperGenerator;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.fabric.FabricContractWrapper;
import org.blockchainnative.fabric.FabricContractWrapperGenerator;
import org.blockchainnative.fabric.FabricUser;
import org.blockchainnative.fabric.typeconverters.FabricDefaultTypeConverters;
import org.blockchainnative.spring.autoconfigure.CoreAutoConfiguration;
import org.blockchainnative.util.StringUtil;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Registers beans required for using blockchain-native applications with Hyperledger Fabric.
 *
 * @since 1.0
 * @author Matthias Veit
 */
@Configuration
@ConditionalOnClass(FabricContractWrapper.class)
@Import(CoreAutoConfiguration.class)
@EnableConfigurationProperties(FabricProperties.class)
public class FabricAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricAutoConfiguration.class);

    private final FabricProperties properties;

    @Autowired
    public FabricAutoConfiguration(@Autowired(required = false) FabricProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates a {@link ContractWrapperGenerator} which creates wrapper classes for {@link org.blockchainnative.fabric.metadata.FabricContractInfo}
     * objects.
     *
     * @param fabricClientFactory {@link Supplier} creating {@link HFClient} objects used to communicate with Hyperledger Fabric networks
     * @param channelFactory {@link Function} creating Channel objects to communicate on. An instance created by {@code fabricClientFactory} is passed to the {@code Function}.
     * @param typeConverters Additional typeconverters to be used
     * @return {@link FabricContractWrapperGenerator}
     */
    @Bean
    @ConditionalOnMissingBean
    public ContractWrapperGenerator fabricContractWrapperGenerator(
            @Autowired Supplier<HFClient> fabricClientFactory,
            @Autowired Function<HFClient, Channel> channelFactory,
            @Autowired TypeConverters typeConverters) {

        return new FabricContractWrapperGenerator(fabricClientFactory, channelFactory, typeConverters);
    }

    /**
     * Registers the {@link org.blockchainnative.convert.TypeConverter} defined by {@link FabricDefaultTypeConverters} as beans.
     *
     * @return {@code BeanDefinitionRegistryPostProcessor} registering the type converters.
     */
    @Bean
    public static BeanDefinitionRegistryPostProcessor fabricDefaultTypeConverters(){
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                for (var typeConverter : FabricDefaultTypeConverters.getConverters()) {
                    LOGGER.info("Registering TypeConverter '{}'", typeConverter.getClass().getName());
                    beanFactory.registerSingleton(typeConverter.getClass().getName(), typeConverter);
                }

            }
        };
    }

    /**
     * Creates a {@link Supplier} creating {@link HFClient} objects. <br>
     * The supplier returns the same instance on each invocation.
     *
     * @return Supplier creating the client to communicate with Hyperledger Fabric
     * @throws IOException
     */
    @Bean
    @ConditionalOnMissingBean
    public Supplier<HFClient> fabricClientFactory() throws IOException {
        LOGGER.info("Creating fabricClientFactory");

        final var user = getDefaultUser();
        return () -> {
            try {
                CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
                var client = HFClient.createNewInstance();
                client.setCryptoSuite(cryptoSuite);
                client.setUserContext(user);
                return client;
            }catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | InvalidArgumentException | CryptoException | ClassNotFoundException e) {
                throw new BeanCreationException("Failed to initialize HFClient for Hyperledger Fabric client facotry.", e);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public Function<HFClient, Channel> channelFactory(@Autowired NetworkConfig networkConfig) {
        LOGGER.info("Creating channelFactory");

        final var channelName = properties.getChannel();

        return (client) -> {
            try {
                var channel = client.loadChannelFromConfig(channelName, networkConfig);
                channel.initialize();
                return channel;
            }catch (Exception e){
                throw new BeanCreationException("Failed to initialize channel for Hyperledger Fabric channel facotry.", e);
            }
        };
    }

    /**
     * Parses and registers the network config file specified in the configuration.
     *
     * @return parsed {@code NetworkConfig}
     * @throws IOException in case the network config file could not be loaded
     * @throws NetworkConfigurationException in case there is an error parsing the
     * @throws InvalidArgumentException in case the network config file is null
     */
    @Bean
    @ConditionalOnMissingBean
    public NetworkConfig networkConfig() throws IOException, NetworkConfigurationException, InvalidArgumentException {
        if(StringUtil.isNullOrEmpty(properties.getNetworkConfigFile())){
            LOGGER.error("NetworkConfig file name is not defined!");
            throw new BeanCreationException("Cannot create Hyperledger Fabric NetworkConfig, network config file name is not defined!");
        }

        LOGGER.info("Creating NetworkConfig from file '{}'", properties.getNetworkConfigFile());

        if(properties.getNetworkConfigFile().endsWith(".json")){
            return NetworkConfig.fromJsonFile(resolveFile(properties.getNetworkConfigFile()));
        } else {
            return NetworkConfig.fromYamlFile(resolveFile(properties.getNetworkConfigFile()));
        }


    }

    private User getDefaultUser() throws IOException {
        if(properties.getUser() == null){
            LOGGER.error("Default user not specified in configuration!");
            throw new BeanCreationException("Default user not specified in configuration!");
        }

        LOGGER.debug("Creating default user: name='{}', roles='{}', account='{}', certificateFile='{}', privateKeyFile='{}', affiliation='{}', mspId='{}'.", properties.getUser().getName(),
                properties.getUser().getRoles() == null ? "" : properties.getUser().getRoles().stream().collect(Collectors.joining(", ")),
                properties.getUser().getAccount(),
                properties.getUser().getAffiliation(),
                properties.getUser().getCertificateFile(),
                properties.getUser().getPrivateKeyFile(),
                properties.getUser().getMspId());

        var certificateFile = resolveFile(properties.getUser().getCertificateFile());
        var privateKeyFile = resolveFile(properties.getUser().getPrivateKeyFile());

        return new FabricUser(
                properties.getUser().getName(),
                properties.getUser().getRoles(),
                properties.getUser().getAccount(),
                properties.getUser().getAffiliation(),
                FabricUser.createEnrollment(certificateFile, privateKeyFile),
                properties.getUser().getMspId());
    }

    private static File resolveFile(String fileName) throws IOException {
        if (fileName.startsWith("classpath:")) {
            return new ClassPathResource(fileName.replace("classpath:", "")).getFile();
        } else {
            return new File(fileName);
        }
    }
}
