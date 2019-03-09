package org.blockchainnative;

import org.blockchainnative.exceptions.ContractWrapperCreationException;
import org.blockchainnative.metadata.ContractInfo;

/**
 * Used to generate wrapper classes for calling smart contracts represented by {@link org.blockchainnative.metadata.ContractInfo} objects.
 *
 * @see org.blockchainnative.annotations.SmartContract
 * @see ContractInfo
 * @author Matthias Veit
 * @since 1.0
 */
public interface ContractWrapperGenerator {

    /**
     * Generates a wrapper class for calling the smart contract represented by the given contract info.
     *
     * @param contractInfo    contract info representing the smart contract
     * @param <TContractInfo> concrete type of contract info expected by the {@code ContractWrapperGenerator}
     * @param <TContractType> Java interface declaring the smart contracts methods and events
     * @return implementation of the given smart contract interface {@code TContractType}
     * @throws java.lang.IllegalArgumentException in case the given contract info is null or not supported by the {@code ContractWrapperGenerator}, {@code TContractType} not an interface type
     * @throws ContractWrapperCreationException   in case an error occurs while creating the wrapper class
     */
    <TContractInfo extends ContractInfo<TContractType, ?, ?>, TContractType> TContractType generate(TContractInfo contractInfo);

    /**
     * Returns the full name a of the smart contract wrapper class represented by a the given contract info.
     *
     * @param contractInfo contract info representing the smart contract
     * @return package name + class name of the generated wrapper class
     */
    default String getWrapperName(ContractInfo contractInfo) {
        var simplifiedIdentifier = contractInfo.getIdentifier().replaceAll("[^a-zA-Z0-9_\\-]", "");
        return String.format("%s.wrapper.%sWrapper_%s", this.getClass().getPackageName(), contractInfo.getClass().getSimpleName(), simplifiedIdentifier);
    }
}
