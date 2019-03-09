package org.blockchainnative.quorum.serialization;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.blockchainnative.quorum.metadata.*;

/**
 * Modifies the serialization and deserialization of the {@link QuorumContractInfo} hierarchy
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @see QuorumContractInfo
 * @see QuorumMethodInfo
 * @see QuorumParameterInfo
 * @see QuorumEventInfo
 * @see QuorumEventFieldInfo
 * @see QuorumEventParameterInfo
 *
 * @since 1.1
 * @author Matthias Veit
 */
public class QuorumMetadataModule extends SimpleModule {

    public QuorumMetadataModule() {
        super("QuorumMetadataModule", new Version(1,0, 0, null, null, null));

        this.setMixInAnnotation(QuorumContractInfo.class, QuorumContractInfoMixin.class);
        this.setMixInAnnotation(QuorumMethodInfo.class, QuorumMethodInfoMixin.class);
        this.setMixInAnnotation(QuorumParameterInfo.class, QuorumParameterInfoMixin.class);
        this.setMixInAnnotation(QuorumEventInfo.class, QuorumEventInfoMixin.class);
        this.setMixInAnnotation(QuorumEventFieldInfo.class, QuorumEventFieldInfoMixin.class);
        this.setMixInAnnotation(QuorumEventParameterInfo.class, QuorumEventParameterInfoMixin.class);
    }
}
