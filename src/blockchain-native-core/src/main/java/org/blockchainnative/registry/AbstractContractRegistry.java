package org.blockchainnative.registry;

import org.blockchainnative.metadata.ContractInfo;
import org.blockchainnative.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Base type for handling multiple {@code ContractInfo}.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public abstract class AbstractContractRegistry implements ContractRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContractRegistry.class);

    protected Map<String, ContractInfo> contractInfos;

    /**
     * Creates a new {@code ContractRegistry}.
     */
    public AbstractContractRegistry() {
        this.contractInfos = new HashMap<>();
    }

    /**
     * Retrieves a {@code ContractInfo} from the registry and returns it as as the expected type parameter.
     *
     * @param contractIdentifier identifier of the contract to be retrieved.
     * @param <T>                Concrete type of the {@code ContractInfo}.
     * @return {@code ContractInfo}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ContractInfo> T getContractInfo(String contractIdentifier) {
        if (StringUtil.isNullOrEmpty(contractIdentifier))
            throw new IllegalArgumentException("Contract identifier must not be null or empty!");

        var contractInfo = this.contractInfos.get(contractIdentifier);
        try {
            return (T) contractInfo;
        } catch (ClassCastException e) {
            var message = String.format("Failed to cast contract info '%s' (%s) to expected type.", contractIdentifier, contractInfo.getClass());
            LOGGER.error(message, e);
            throw new IllegalStateException(message, e);
        }
    }

    /**
     * Adds a {@code ContractInfo} to the registry
     *
     * @param contractInfo {@code ContractInfo} to be added.
     * @throws IllegalStateException in case the registry already contains a {@code ContractInfo} with the same identifier
     */
    @Override
    public void addContractInfo(ContractInfo contractInfo) {
        if (contractInfo == null) throw new IllegalArgumentException("ContractInfo must not be null");
        if (StringUtil.isNullOrEmpty(contractInfo.getIdentifier()))
            throw new IllegalArgumentException("Contract identifier must not be null or empty!");

        if (this.contractInfos.containsKey(contractInfo.getIdentifier())) {
            var message = String.format("ContractRegistry already contains a ContractInfo with key '%s'.", contractInfo.getIdentifier());
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }

        this.contractInfos.put(contractInfo.getIdentifier(), contractInfo);
    }

    /**
     * Adds a {@code ContractInfo} to the registry or replaces the same in case the registry already contains a {@code ContractInfo} with the same identifier
     *
     * @param contractInfo {@code ContractInfo} to be added or updated.
     */
    @Override
    public void addOrUpdateContractInfo(ContractInfo contractInfo) {
        if (contractInfo == null) throw new IllegalArgumentException("ContractInfo must not be null");
        if (StringUtil.isNullOrEmpty(contractInfo.getIdentifier()))
            throw new IllegalArgumentException("Contract identifier must not be null or empty!");

        this.contractInfos.put(contractInfo.getIdentifier(), contractInfo);
    }

    /**
     * Adds a {@code ContractInfo} to the registry in case the registry does not already contain a {@code ContractInfo} with the same identifier
     *
     * @param contractInfo {@code ContractInfo} to be added.
     */
    @Override
    public void addContractInfoIfNotExisting(ContractInfo contractInfo) {
        if (contractInfo == null) throw new IllegalArgumentException("ContractInfo must not be null");
        if (StringUtil.isNullOrEmpty(contractInfo.getIdentifier()))
            throw new IllegalArgumentException("Contract identifier must not be null or empty!");

        if (!this.contractInfos.containsKey(contractInfo.getIdentifier())) {
            this.contractInfos.put(contractInfo.getIdentifier(), contractInfo);
        }
    }

    /**
     * Checks whether or not a {@code ContractInfo} is already registered.
     *
     * @param contractInfo {@code ContractInfo} to be checked
     * @return flag indicating whether or not the given {@code ContractInfo} is already registered.
     */
    @Override
    public boolean isRegistered(ContractInfo contractInfo) {
        if (contractInfo == null) throw new IllegalArgumentException("ContractInfo must not be null");

        return isRegistered(contractInfo.getIdentifier());
    }

    /**
     * Checks whether or not a {@code ContractInfo} is already registered.
     *
     * @param contractIdentifier identifier of the {@code ContractInfo} to be checked
     * @return flag indicating whether or not the given {@code ContractInfo} is already registered.
     */
    @Override
    public boolean isRegistered(String contractIdentifier) {
        if (StringUtil.isNullOrEmpty(contractIdentifier))
            throw new IllegalArgumentException("Contract identifier must not be null or empty!");

        return contractInfos.containsKey(contractIdentifier);
    }

    /**
     * Returns all {@code ContractInfo} registered with this {@code ContractRegistry}.
     *
     * @return List containing all {@code ContractInfo} registered with this {@code ContractRegistry}.
     */
    @Override
    public Collection<? extends ContractInfo> getContractInfos() {
        return new ArrayList<>(this.contractInfos.values());
    }


}
