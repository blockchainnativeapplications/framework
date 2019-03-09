package org.blockchainnative.ethereum.serialization;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.blockchainnative.ethereum.metadata.*;

/**
 * Modifies the serialization and deserialization of the {@link EthereumContractInfo} hierarchy
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @see EthereumContractInfo
 * @see EthereumMethodInfo
 * @see EthereumParameterInfo
 * @see EthereumEventInfo
 * @see EthereumEventFieldInfo
 * @see EthereumEventParameterInfo
 *
 * @since 1.0
 * @author Matthias Veit
 */
public class EthereumMetadataModule extends SimpleModule {

    public EthereumMetadataModule() {
        super("EthereumMetadataModule", new Version(1,0, 0, null, null, null));

        this.setMixInAnnotation(EthereumContractInfo.class, EthereumContractInfoMixin.class);
        this.setMixInAnnotation(EthereumMethodInfo.class, EthereumMethodInfoMixin.class);
        this.setMixInAnnotation(EthereumParameterInfo.class, EthereumParameterInfoMixin.class);
        this.setMixInAnnotation(EthereumEventInfo.class, EthereumEventInfoMixin.class);
        this.setMixInAnnotation(EthereumEventFieldInfo.class, EthereumEventFieldInfoMixin.class);
        this.setMixInAnnotation(EthereumEventParameterInfo.class, EthereumEventParameterInfoMixin.class);
    }
}
