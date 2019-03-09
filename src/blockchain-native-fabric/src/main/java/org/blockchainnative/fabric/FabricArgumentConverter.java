package org.blockchainnative.fabric;

import org.blockchainnative.fabric.metadata.FabricEventInfo;
import org.blockchainnative.fabric.metadata.FabricMethodInfo;
import org.blockchainnative.fabric.metadata.FabricParameterInfo;

import java.util.List;

/**
 * Internal API for converting arguments to pass them to Hyperledger Fabric chaincode methods.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public interface FabricArgumentConverter {

    /**
     * Creates the object object from the chaincode event data.
     *
     * @param eventInfo event info
     * @param eventData chaincode event data
     * @return extracted event object
     */
    Object createEventObject(FabricEventInfo eventInfo, Object eventData);

    /**
     * Converts the parameters of a method call to a list of {@code String}.
     *
     * @param parameterInfos parameter infos
     * @param arguments      parameter values
     * @return list of string containing the converted arguments
     */
    List<String> convertArguments(List<FabricParameterInfo> parameterInfos, Object[] arguments);


    /**
     * Converts the results of a method call to the declared result type
     *
     * @param methodInfo method info
     * @param output     chaincode result
     * @return converted method result
     */
    Object convertMethodResult(FabricMethodInfo methodInfo, Object output);
}
