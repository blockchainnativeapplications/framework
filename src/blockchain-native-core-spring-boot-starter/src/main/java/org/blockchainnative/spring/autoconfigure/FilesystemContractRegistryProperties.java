package org.blockchainnative.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author Matthias Veit
 */
@ConfigurationProperties(prefix = FilesystemContractRegistryProperties.CONTRACT_REGISTRY_PREFIX_FULL)
public class FilesystemContractRegistryProperties {
    public static final String CONTRACT_REGISTRY_PREFIX = "contractregistry";
    public static final String CONTRACT_REGISTRY_PREFIX_FULL = Constants.CONFIGURATION_PREFIX + "." + CONTRACT_REGISTRY_PREFIX;

    private String provider;
    private Path basePath;

    @PostConstruct
    private void postConstruct(){
        if(this.provider == null){
            this.provider = Constants.FILESYSTEM_PROVIDER;
        }
        if(this.basePath == null){
            this.basePath = Paths.get("contracts").toAbsolutePath();
        }
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Path getBasePath() {
        return basePath;
    }

    public void setBasePath(Path basePath) {
        this.basePath = basePath;
    }
}
