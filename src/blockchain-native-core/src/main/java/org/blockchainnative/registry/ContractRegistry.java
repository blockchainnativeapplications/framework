package org.blockchainnative.registry;

import org.blockchainnative.metadata.ContractInfo;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Matthias Veit
 */
public interface ContractRegistry {
    @SuppressWarnings("unchecked")
    <T extends ContractInfo> T getContractInfo(String contractIdentifier);

    void addContractInfo(ContractInfo contractInfo);

    void addOrUpdateContractInfo(ContractInfo contractInfo);

    void addContractInfoIfNotExisting(ContractInfo contractInfo);

    boolean isRegistered(ContractInfo contractInfo);

    boolean isRegistered(String contractIdentifier);

    Collection<? extends ContractInfo> getContractInfos();

    /**
     * Saves all {@code ContractInfo} registered with this {@code ContractRegistry}.
     * How and where the {@code ContractInfo} objects are stored depends on the concrete implementation.
     *
     * @throws IOException in case of errors during persisting the contract infos
     */
    void persist() throws IOException;

    /**
     * Loads previously persisted {@code ContractInfo}.
     * How and where the {@code ContractInfo} objects are loaded from depends on the concrete implementation.
     *
     * @throws IOException in case of errors during loading the contract infos
     */
    void load() throws IOException;
}
