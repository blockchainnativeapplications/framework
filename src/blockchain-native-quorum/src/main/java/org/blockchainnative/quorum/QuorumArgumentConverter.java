package org.blockchainnative.quorum;

import org.blockchainnative.quorum.metadata.QuorumEventInfo;
import org.blockchainnative.quorum.metadata.QuorumMethodInfo;
import org.blockchainnative.quorum.metadata.QuorumParameterInfo;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.util.List;

/**
 * Internal API for converting Ethereum/Web3j types.
 *
 * @author Matthias Veit
 * @since 1.1
 */
public interface QuorumArgumentConverter {

    /**
     * Creates the object object from the Web3j {@code EventValues}.
     *
     * @param eventInfo   event info
     * @param eventValues web3j event values
     * @return extracted event object
     */
    Object createEventObject(QuorumEventInfo eventInfo, EventValues eventValues);

    /**
     * Converts the parameters of a method call to the corresponding types expected by Web3j.
     *
     * @param parameterInfos parameter infos
     * @param arguments      parameter values
     * @return corresponding types expected by Web3j
     */
    List<Type> convertArguments(List<QuorumParameterInfo> parameterInfos, Object[] arguments);

    /**
     * Converts the results of a method call from the Web3j types to the declared result type
     *
     * @param methodInfo method info
     * @param results    Web3j results
     * @return converted method result
     */
    Object convertMethodResult(QuorumMethodInfo methodInfo, List<Type> results);


    /**
     * Dynamically creates {@code TypeReference} objects for the input parameters of a {@code AbiDefinition}.
     *
     * @param methodAbi abi definition
     * @return list of {@code TypeReference} objects
     */
    List<TypeReference<?>> getInputParameterTypesReferences(AbiDefinition methodAbi);

    /**
     * Dynamically creates {@code TypeReference} objects for the output parameters of a {@code AbiDefinition}.
     *
     * @param methodAbi abi definition
     * @return list of {@code TypeReference} objects
     */
    List<TypeReference<?>> getOutputParameterTypeReferences(AbiDefinition methodAbi);

}
