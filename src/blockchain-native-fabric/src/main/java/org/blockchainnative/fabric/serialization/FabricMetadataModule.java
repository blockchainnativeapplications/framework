package org.blockchainnative.fabric.serialization;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.blockchainnative.fabric.metadata.*;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;

/**
 * Modifies the serialization and deserialization of the {@link FabricContractInfo} hierarchy
 * through {@link com.fasterxml.jackson.databind.ObjectMapper}.
 *
 * @see FabricContractInfo
 * @see FabricMethodInfo
 * @see FabricParameterInfo
 * @see FabricEventInfo
 * @see FabricEventFieldInfo
 * @see FabricEventParameterInfo
 *
 * @since 1.0
 * @author Matthias Veit
 */
public class FabricMetadataModule extends SimpleModule {

    public FabricMetadataModule() {
        super("FabricMetadataModule", new Version(1,0, 0, null, null, null));

        this.addSerializer(ChaincodeID.class, new ChaincodeIDSerializer());
        this.addDeserializer(ChaincodeID.class, new ChaincodeIDDeserializer());

        this.addSerializer(ChaincodeEndorsementPolicy.class, new ChaincodePolicySerializer());
        this.addDeserializer(ChaincodeEndorsementPolicy.class, new ChaincodePolicyDeserializer());

        this.setMixInAnnotation(FabricContractInfo.class, FabricContractInfoMixin.class);
        this.setMixInAnnotation(FabricMethodInfo.class, FabricMethodInfoMixin.class);
        this.setMixInAnnotation(FabricParameterInfo.class, FabricParameterInfoMixin.class);
        this.setMixInAnnotation(FabricEventInfo.class, FabricEventInfoMixin.class);
        this.setMixInAnnotation(FabricEventFieldInfo.class, FabricEventFieldInfoMixin.class);
        this.setMixInAnnotation(FabricEventParameterInfo.class, FabricEventParameterInfoMixin.class);
    }
}
