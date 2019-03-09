package org.blockchainnative.spring.autoconfigure;

import org.blockchainnative.ContractWrapperGenerator;
import org.blockchainnative.convert.TypeConverter;
import org.blockchainnative.convert.TypeConverters;
import org.blockchainnative.registry.ContractRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

import static org.blockchainnative.spring.autoconfigure.ContractFactory.CONTRACT_FACTORY_BEAN_NAME;

/**
 * @author Matthias Veit
 * @since 1.0
 */
@Configuration
public class CoreAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreAutoConfiguration.class);

    /**
     * Registers a {@link ContractFactory} for creating smart contract wrapper classes.
     *
     * @param contractWrapperGenerator blockchain-specific {@code ContractWrapperGenerator}
     * @param contractRegistry  registry holding {@code ContractInfo} objects
     * @return {@link ContractFactory}
     */
    @Bean(CONTRACT_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = CONTRACT_FACTORY_BEAN_NAME)
    public ContractFactory contractFactory(@Autowired ContractWrapperGenerator contractWrapperGenerator, @Autowired(required = false) ContractRegistry contractRegistry){
        LOGGER.info("Registering contract factory");
        return new ContractFactory(contractWrapperGenerator, contractRegistry);
    }

    /**
     * Registers {@link TypeConverters} containing all {@link TypeConverter} found via dependency injection.
     *
     * @param typeConverters type converters to be added
     * @return {@link TypeConverters}
     */
    @Bean
    @ConditionalOnMissingBean(TypeConverters.class)
    public TypeConverters typeConverters(@Autowired(required = false) Collection<? extends TypeConverter<?,?>> typeConverters) {
        if(typeConverters != null) {
            LOGGER.info("Registering type converters, found {} converters", typeConverters.size());
            return new TypeConverters(typeConverters);
        } else {
            LOGGER.info("Registering type converters");
            return new TypeConverters();
        }

    }
}
