package org.blockchainnative.spring.autoconfigure;

import com.fasterxml.jackson.databind.Module;
import org.blockchainnative.metadata.ContractInfo;
import org.blockchainnative.registry.ContractRegistry;
import org.blockchainnative.registry.FileSystemContractRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Creates a {@link FileSystemContractRegistry} and collects all {@link ContractInfo} objects registered for dependency injection.
 * The contract infos stored in the base path of {@code FileSystemContractRegistry} are automatically loaded.
 * Additional contract infos are only added if their identifiers are different form those loaded from the file system.
 *
 * @since 1.0
 * @author Matthias Veit
 */
public abstract class AbstractFilesystemContractRegistryConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFilesystemContractRegistryConfiguration.class);

    protected final FilesystemContractRegistryProperties properties;

    public AbstractFilesystemContractRegistryConfiguration(FilesystemContractRegistryProperties properties) {
        this.properties = properties;
    }

    /**
     * Allows sub classes to register {@link Module} objects before the contract infos are loaded.
     *
     * @return List of Modules to be added
     */
    protected abstract List<Module> getRequiredModules();

    @Bean
    @ConditionalOnProperty(prefix = FilesystemContractRegistryProperties.CONTRACT_REGISTRY_PREFIX_FULL, name = "provider", havingValue = "filesystem", matchIfMissing = true)
    @ConditionalOnMissingBean(ContractRegistry.class)
    public ContractRegistry contractRegistry(@Autowired(required = false) Collection<ContractInfo<?, ?, ?>> contractInfos) throws IOException {
        LOGGER.info("Registering FileSystemContractRegistry with base path '{}' as bean", properties.getBasePath());

        var contractRegistry = new FileSystemContractRegistry(properties.getBasePath());

        // get modules required for (de-)serializing objects
        var modules = this.getRequiredModules();

        if (modules != null) {
            modules.forEach(module -> {
                LOGGER.debug("Registering object mapper module '{}'", module.getModuleName());
                contractRegistry.registerObjectMapperModule(module);
            });

        } else {
            LOGGER.debug("No object mapper modules have been registered.");
        }

        // load contract info objects from file system
        LOGGER.info("Loading contract infos from file system...");
        contractRegistry.load();

        var count = contractRegistry.getContractInfos().size();
        LOGGER.info("Found {} contract info object{}", count, count == 0 || count > 1 ? "s" : "");

        // add registered  contract info objects if not existing on file system
        if (contractInfos != null) {
            LOGGER.info("Adding manually registered contract infos");
            contractInfos.forEach(ci -> {
                if(!contractRegistry.isRegistered(ci)){
                    LOGGER.info("Adding contract info '{}' ({})", ci.getIdentifier(), ci.getClass());
                    contractRegistry.addContractInfo(ci);
                }else {
                    LOGGER.info("Skipping contract info '{}' ({}), a never version has been loaded from file system.", ci.getIdentifier(), ci.getClass());
                }
            });
        }

        return contractRegistry;
    }
}
