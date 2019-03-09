package org.blockchainnative.fabric.metadata;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.metadata.ContractInfo;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Holds all relevant information about smart contract interfaces in order to allow {@link
 * org.blockchainnative.fabric.FabricContractWrapperGenerator} to generate a functioning wrapper class. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.fabric.builder.FabricContractInfoBuilder} to construct instances
 * of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.fabric.FabricContractWrapperGenerator
 * @since 1.0
 */
public class FabricContractInfo<TContractType> extends ContractInfo<TContractType, FabricMethodInfo, FabricEventInfo> {

    private final ChaincodeID chaincodeID;
    private final ChaincodeLanguage chaincodeLanguage;
    private ChaincodeEndorsementPolicy chaincodePolicy;
    private String chaincodeSourceDirectory;
    private Set<String> targetPeerNames;

    private Set<String> instantiatedOnPeers;
    private Set<String> installedOnPeers;

    /**
     * Constructs a new {@code FabricContractInfo}
     *
     * @param identifier               identifier of the smart contract
     * @param contractClass            Java interface representing the Hyperledger chaincode
     * @param methodInfos              {@code FabricMethodInfo} objects
     * @param eventInfos               {@code FabricEventInfo} objects
     * @param chaincodeID              identifier specifying the name, version and path of the chaincode
     * @param chaincodeLanguage        language in which the chaincode is written, required for deployment, may be null
     * @param chaincodePolicy          chaincode policy, required for deployment, may be null
     * @param chaincodeSourceDirectory directory containing the chaincode source files, required for deployment, may be
     *                                 null
     * @param targetPeerNames          name of the peers to target, may be null
     */
    public FabricContractInfo(String identifier, Class<TContractType> contractClass, Map<Method, FabricMethodInfo> methodInfos, Map<String, FabricEventInfo> eventInfos, ChaincodeID chaincodeID, ChaincodeLanguage chaincodeLanguage, ChaincodeEndorsementPolicy chaincodePolicy, String chaincodeSourceDirectory, Set<String> targetPeerNames) {
        super(identifier, contractClass, methodInfos, eventInfos);
        this.chaincodeID = chaincodeID;
        this.chaincodeLanguage = chaincodeLanguage;
        this.chaincodePolicy = chaincodePolicy;
        this.chaincodeSourceDirectory = chaincodeSourceDirectory;
        this.targetPeerNames = targetPeerNames;
    }

    /**
     * Constructs a new {@code FabricContractInfo}
     *
     * @param identifier               identifier of the smart contract
     * @param contractClass            Java interface representing the Hyperledger chaincode
     * @param methodInfos              {@code FabricMethodInfo} objects
     * @param eventInfos               {@code FabricEventInfo} objects
     * @param chaincodeID              identifier specifying the name, version and path of the chaincode
     * @param chaincodeLanguage        language in which the chaincode is written, required for deployment, may be null
     * @param chaincodePolicy          chaincode policy, required for deployment, may be null
     * @param chaincodeSourceDirectory directory containing the chaincode source files, required for deployment, may be
     *                                 null
     * @param targetPeerNames          name of the peers to target, may be null
     */
    public FabricContractInfo(String identifier, Class<TContractType> contractClass, Collection<FabricMethodInfo> methodInfos, Collection<FabricEventInfo> eventInfos, ChaincodeID chaincodeID, ChaincodeLanguage chaincodeLanguage, ChaincodeEndorsementPolicy chaincodePolicy, String chaincodeSourceDirectory, Set<String> targetPeerNames) {
        super(identifier, contractClass, methodInfos, eventInfos);
        this.chaincodeID = chaincodeID;
        this.chaincodeLanguage = chaincodeLanguage;
        this.chaincodePolicy = chaincodePolicy;
        this.chaincodeSourceDirectory = chaincodeSourceDirectory;
        this.targetPeerNames = targetPeerNames;
    }

    /**
     * Returns the {@code ChaincodeID} specifying the name, version and path of the Hyperledger chaincode.
     *
     * @return {@code ChaincodeID} of the Hyperledger chaincode.
     */
    public ChaincodeID getChaincodeID() {
        return chaincodeID;
    }

    /**
     * Returns the {@code ChaincodeLanguage} of the Hyperledger chaincode.
     *
     * @return {@code ChaincodeLanguage} of the Hyperledger chaincode.
     */
    public ChaincodeLanguage getChaincodeLanguage() {
        return chaincodeLanguage;
    }

    /**
     * Returns the names of the peers to be targeted by the Hyperledger chaincode's methods.
     *
     * @return names of the peers to be targeted by the Hyperledger chaincode
     */
    public Set<String> getTargetPeerNames() {
        return targetPeerNames;
    }

    /**
     * Sets the names of the peers to be targeted by the Hyperledger chaincode's methods.
     *
     * @param targetPeerNames names of the peers to be targeted by the Hyperledger chaincode
     */
    public void setTargetPeerNames(Set<String> targetPeerNames) {
        this.targetPeerNames = targetPeerNames;
    }

    /**
     * Returns the directory containing the chaincode source files.
     *
     * @return directory containing the chaincode source files.
     */
    public String getChaincodeSourceDirectory() {
        return chaincodeSourceDirectory;
    }

    /**
     * Sets the directory containing the chaincode source files.
     *
     * @param chaincodeSourceDirectory directory containing the chaincode source files.
     */
    public void setChaincodeSourceDirectory(String chaincodeSourceDirectory) {
        this.chaincodeSourceDirectory = chaincodeSourceDirectory;
    }

    /**
     * Returns the chaincode's endorsement policy.
     *
     * @return the chaincode's endorsement policy.
     */
    public ChaincodeEndorsementPolicy getChaincodePolicy() {
        return chaincodePolicy;
    }

    /**
     * Sets the chaincode's endorsement policy.
     *
     * @param chaincodePolicy the chaincode's endorsement policy.
     */
    public void setChaincodePolicy(ChaincodeEndorsementPolicy chaincodePolicy) {
        this.chaincodePolicy = chaincodePolicy;
    }

    /**
     * Adds a peer to set of peers to which to chaincode is considered to be installed. <br>
     * Beware that this does not necessarily reflect the actual state of the contract.
     *
     * @param peerName name of the peer
     */
    public void addInstalledOn(String peerName){
        if(installedOnPeers == null){
            installedOnPeers = new HashSet<>();
        }
        installedOnPeers.add(peerName);
    }

    /**
     * Sets the peer names to which the chaincode is considered to be installed. <br>
     * Beware that this does not necessarily reflect the actual state of the contract.
     *
     * @param peerNames set of peer names
     */
    public void setInstalledOn(Set<String> peerNames){
        this.installedOnPeers = peerNames != null ? new HashSet<>(peerNames) : null;
    }

    /**
     * Sets the peer names on which the chaincode is considered to be instantiated. <br>
     * Beware that this does not necessarily reflect the actual state of the contract.
     *
     * @param peerNames set of peer names
     */
    public void setInstantiatedOn(Set<String> peerNames){
        this.instantiatedOnPeers = peerNames != null ? new HashSet<>(peerNames) : null;
    }

    /**
     * Returns the peer names on which the chaincode is considered to be installed. <br>
     * Beware that this does not necessarily reflect the actual state of the contract.
     *
     * @return set of peer names
     */
    public Set<String> getInstalledOnPeers(){
        return installedOnPeers != null ? Collections.unmodifiableSet(installedOnPeers) : null;
    }

    /**
     * Returns the peer names on which the chaincode is considered to be instantiated. <br>
     * Beware that this does not necessarily reflect the actual state of the contract.
     *
     * @return set of peer names
     */
    public Set<String> getInstantiatedOnPeers(){
        return installedOnPeers != null ? Collections.unmodifiableSet(instantiatedOnPeers) : null;
    }

    /**
     * Checks whether the chaincode is considered to be installed on a specific peer. <br>
     * Beware that this does not necessarily reflect the actual state of the contract.
     *
     * @param peerName name of the peer to check
     * @return flag indicating the assumed chaincode's installation status on the given peer
     */
    public boolean isInstalledOn(String peerName){
        return installedOnPeers != null && installedOnPeers.contains(peerName);
    }

    /**
     * Checks whether the chaincode is considered to be instantiated. <br>
     * Beware that this does not necessarily reflect the actual state of the contract.
     *
     * @return flag indicating the assumed chaincode's instantiation status
     */
    public boolean isInstantiated(){
        return instantiatedOnPeers != null && !instantiatedOnPeers.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof FabricContractInfo)) return false;

        FabricContractInfo<?> that = (FabricContractInfo<?>) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(chaincodeID != null ? chaincodeID.toString() : null, that.chaincodeID != null ? that.chaincodeID.toString() : null)
                .append(chaincodeLanguage, that.chaincodeLanguage)
                .append(chaincodePolicy != null ? chaincodePolicy.getChaincodeEndorsementPolicyAsBytes() : null, that.chaincodePolicy != null ? that.chaincodePolicy.getChaincodeEndorsementPolicyAsBytes() : null)
                .append(chaincodeSourceDirectory, that.chaincodeSourceDirectory)
                .append(targetPeerNames, that.targetPeerNames)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(chaincodeID)
                .append(chaincodeLanguage)
                .append(chaincodePolicy)
                .append(chaincodeSourceDirectory)
                .append(targetPeerNames)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("chaincodeID", chaincodeID)
                .append("chaincodeLanguage", chaincodeLanguage)
                .toString();
    }
}
