package org.blockchainnative.spring.autoconfigure;

import org.blockchainnative.ContractWrapperGenerator;
import org.blockchainnative.metadata.ContractInfo;
import org.blockchainnative.registry.ContractRegistry;
import org.springframework.beans.factory.BeanCreationException;

/**
 * @author Matthias Veit
 */

public class ContractFactory {

    public static final String CONTRACT_FACTORY_BEAN_NAME = "contractFactory";
    public static final String CONTRACT_FACTORY_METHOD_NAME = "createContract";

    private final ContractWrapperGenerator contractWrapperGenerator;
    private final ContractRegistry contractRegistry;

    public ContractFactory(ContractWrapperGenerator contractWrapperGenerator, ContractRegistry contractRegistry) {
        this.contractWrapperGenerator = contractWrapperGenerator;
        this.contractRegistry = contractRegistry;
    }

    @SuppressWarnings("unchecked")
    public <T> T createContract(ContractInfo<T, ?, ?> contractInfo) {
        try {
            return (T) contractWrapperGenerator.generate(getLatestContractInfo(contractInfo));
        } catch (ClassCastException e) {
            throw new BeanCreationException("Failed to cast contract wrapper to specified type!", e);
        }
    }

    private ContractInfo getLatestContractInfo(ContractInfo contractInfo) {
        if (this.contractRegistry != null) {
            var registryContractInfo = this.contractRegistry.getContractInfo(contractInfo.getIdentifier());

            return registryContractInfo != null ? registryContractInfo : contractInfo;
        } else {
            return contractInfo;
        }
    }
}
