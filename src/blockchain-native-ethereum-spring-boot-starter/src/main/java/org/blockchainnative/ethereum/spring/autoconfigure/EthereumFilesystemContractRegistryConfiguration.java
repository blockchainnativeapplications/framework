package org.blockchainnative.ethereum.spring.autoconfigure;

import com.fasterxml.jackson.databind.Module;
import org.blockchainnative.ethereum.serialization.EthereumMetadataModule;
import org.blockchainnative.metadata.ContractInfo;
import org.blockchainnative.registry.ContractRegistry;
import org.blockchainnative.registry.FileSystemContractRegistry;
import org.blockchainnative.spring.autoconfigure.AbstractFilesystemContractRegistryConfiguration;
import org.blockchainnative.spring.autoconfigure.FilesystemContractRegistryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Matthias Veit
 */
@Configuration
@ConditionalOnClass(ContractRegistry.class)
@EnableConfigurationProperties(FilesystemContractRegistryProperties.class)
public class EthereumFilesystemContractRegistryConfiguration extends AbstractFilesystemContractRegistryConfiguration {

    @Autowired
    public EthereumFilesystemContractRegistryConfiguration(FilesystemContractRegistryProperties properties) {
        super(properties);
    }

    @Override
    protected List<Module> getRequiredModules() {
        return new ArrayList<>(){{
            add(new EthereumMetadataModule());
        }};
    }
}
