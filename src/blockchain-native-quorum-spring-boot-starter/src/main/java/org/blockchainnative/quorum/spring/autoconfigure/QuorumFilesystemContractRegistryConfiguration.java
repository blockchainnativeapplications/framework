package org.blockchainnative.quorum.spring.autoconfigure;

import com.fasterxml.jackson.databind.Module;
import org.blockchainnative.ethereum.serialization.EthereumMetadataModule;
import org.blockchainnative.quorum.serialization.QuorumMetadataModule;
import org.blockchainnative.registry.ContractRegistry;
import org.blockchainnative.spring.autoconfigure.AbstractFilesystemContractRegistryConfiguration;
import org.blockchainnative.spring.autoconfigure.FilesystemContractRegistryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Veit
 */
@Configuration
@ConditionalOnClass(ContractRegistry.class)
@EnableConfigurationProperties(FilesystemContractRegistryProperties.class)
public class QuorumFilesystemContractRegistryConfiguration extends AbstractFilesystemContractRegistryConfiguration {

    @Autowired
    public QuorumFilesystemContractRegistryConfiguration(FilesystemContractRegistryProperties properties) {
        super(properties);
    }

    @Override
    protected List<Module> getRequiredModules() {
        return new ArrayList<>(){{
            add(new QuorumMetadataModule());
        }};
    }
}
