package org.blockchainnative.quorum;

import org.blockchainnative.SmartContract;
import org.blockchainnative.quorum.metadata.QuorumContractInfo;

/**
 * Allows smart contract wrappers to retrieve their contract info.
 * <p>
 * The use of this interface is optional, declaring a method named {@link SmartContract#GET_CONTRACT_INFO_METHOD_NAME}
 * with the return type of {@link QuorumContractInfo} (with or without {@code TContractType}) will have the same result.
 * </p>
 *
 * @param <TContractType> Type parameter representing the actual contract type.
 *
 * @since 1.1
 * @author Matthias Veit
 */
public interface QuorumSmartContract<TContractType> extends SmartContract<QuorumContractInfo<TContractType>> {
}
