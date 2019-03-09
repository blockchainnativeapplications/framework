package org.blockchainnative;

import org.blockchainnative.metadata.ContractInfo;

/**
 * Optional interface to implemented by smart contract wrapper interfaces.
 *
 * <p>
 * The use of this interface is optional, declaring a method named {@link SmartContract#GET_CONTRACT_INFO_METHOD_NAME}
 * with the return type of {@link ContractInfo} (or the correct subtype with or without {@code TContractType}) will have the same result.
 * </p>
 *
 * @since 1.0
 * @author Matthias Veit
 */
public interface SmartContract<TContractInfo extends ContractInfo> {
    String GET_CONTRACT_INFO_METHOD_NAME = "getContractInfo";

    /**
     * Returns the {@code ContractInfo} representing this contract.
     *
     * @return {@code ContractInfo}
     */
    TContractInfo getContractInfo();
}
