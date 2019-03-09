package org.blockchainnative.fabric.metadata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.metadata.EventInfo;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Holds additional information about a smart contract event. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.fabric.builder.FabricContractInfoBuilder} to construct instances
 * of this class.
 * <p>
 * Since Hyperledger Fabric chaincode events only contain a single payload, only one {@code FabricEventFieldInfo} is
 * supported per {@code FabricEventInfo}.
 * </p>
 *
 *
 * @author Matthias Veit
 * @see org.blockchainnative.fabric.builder.FabricContractInfoBuilder
 * @since 1.0
 */
public class FabricEventInfo extends EventInfo<FabricEventFieldInfo, FabricEventParameterInfo> {

    /**
     * Constructs a new {@code FabricEventInfo}
     *
     * @param name                      name of the event
     * @param method                    method on the contract interface representing the event
     * @param fabricEventParameterInfos {@code FabricEventParameterInfo} objects
     * @param fabricEventFieldInfos     {@code FabricEventFieldInfo} objects
     */
    public FabricEventInfo(String name, Method method, Collection<FabricEventParameterInfo> fabricEventParameterInfos, Collection<FabricEventFieldInfo> fabricEventFieldInfos) {
        super(name, method, fabricEventParameterInfos, fabricEventFieldInfos);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
