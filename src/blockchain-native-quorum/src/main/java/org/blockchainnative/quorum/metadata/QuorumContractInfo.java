package org.blockchainnative.quorum.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.blockchainnative.metadata.ContractInfo;
import org.blockchainnative.util.StringUtil;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Holds all relevant information about smart contract interfaces in order to allow {@link
 * org.blockchainnative.ethereum.EthereumContractWrapperGenerator} to generate a functioning wrapper class. <br>
 * <br>
 * It is recommended to use {@link org.blockchainnative.quorum.builder.QuorumContractInfoBuilder} to construct
 * instances of this class.
 *
 * @author Matthias Veit
 * @see org.blockchainnative.quorum.QuorumContractWrapperGenerator
 * @since 1.1
 */
public class QuorumContractInfo<TContractType> extends ContractInfo<TContractType, QuorumMethodInfo, QuorumEventInfo> {
    private String contractAddress;
    private String abi;
    private String binary;
    private List<String> privateFor;
    private AbiDefinition[] abiDefinitions;

    /**
     * Constructs a new {@code QuorumContractInfo}
     *
     * @param identifier      identifier of the smart contract
     * @param contractClass   Java interface representing the Quorum smart contract
     * @param methodInfos     {@code QuorumMethodInfo} objects
     * @param eventInfos      {@code QuorumEventInfo} objects
     * @param contractAddress address of the Quorum smart contract or null if it has not been deployed yet
     * @param abi             JSON string containing the application binary interface (ABI) of the Quorum smart
     *                        contract
     * @param binary          Hex string containing the binary of the Quorum smart contract
     * @param privateFor      List of public keys of the nodes addressed by transactions performed in the context of
     *                        this contract info, or null if the transactions shall be public
     */
    public QuorumContractInfo(String identifier, Class<TContractType> contractClass, Collection<QuorumMethodInfo> methodInfos, Collection<QuorumEventInfo> eventInfos, String contractAddress, String abi, String binary, List<String> privateFor) {
        super(identifier, contractClass, methodInfos, eventInfos);
        this.contractAddress = contractAddress;

        this.abiDefinitions = parseContractAbi(abi);
        this.abi = normalizeAbiString(abi);

        this.binary = binary;

        this.privateFor = privateFor;
    }

    /**
     * Constructs a new {@code QuorumContractInfo}
     *
     * @param identifier      identifier of the smart contract
     * @param contractClass   Java interface representing the Quorum smart contract
     * @param methodInfos     {@code QuorumMethodInfo} objects
     * @param eventInfos      {@code QuorumEventInfo} objects
     * @param contractAddress address of the Quorum smart contract or null if it has not been deployed yet
     * @param abi             JSON string containing the application binary interface (ABI) of the Quorum smart
     *                        contract
     * @param binary          Hex string containing the binary of the Quorum smart contract
     * @param privateFor      List of public keys of the nodes addressed by transactions performed in the context of
     *                        this contract info, or null if the transactions shall be public
     */
    public QuorumContractInfo(String identifier, Class<TContractType> contractClass, Map<Method, QuorumMethodInfo> methodInfos, Map<String, QuorumEventInfo> eventInfos, String contractAddress, String abi, String binary, List<String> privateFor) {
        this(identifier, contractClass, methodInfos.values(), eventInfos.values(), contractAddress, abi, binary, privateFor);
    }

    /**
     * Returns the contract address of the Quorum smart contract
     *
     * @return contract address of the Quorum smart contract
     */
    public String getContractAddress() {
        return contractAddress;
    }

    /**
     * Sets the contract address of the Quorum smart contract
     *
     * @param contractAddress contract address of the Quorum smart contract
     */
    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    /**
     * Returns the JSON string containing the application binary interface (ABI) of the Quorum smart contract
     *
     * @return JSON string containing the application binary interface (ABI) of the Quorum smart contract
     */
    public String getAbi() {
        return abi;
    }

    /**
     * Returns the parsed application binary interface (ABI) of the Quorum smart contract
     *
     * @return parsed application binary interface (ABI) of the Quorum smart contract
     */
    public AbiDefinition[] getAbiDefinitions() {
        return abiDefinitions;
    }

    /**
     * Sets the JSON string containing the application binary interface (ABI) of the Quorum smart contract
     *
     * @param abi JSON string containing the application binary interface (ABI) of the Quorum smart contract
     */
    public void setAbi(String abi) {
        this.abiDefinitions = parseContractAbi(abi);
        this.abi = normalizeAbiString(abi);
    }

    /**
     * Returns the hex string containing the binary of the Quorum smart contract
     *
     * @return Hex string containing the binary of the Quorum smart contract
     */
    public String getBinary() {
        return binary;
    }

    /**
     * Sets the hex string containing the binary of the Quorum smart contract
     *
     * @param binary Hex string containing the binary of the Quorum smart contract
     */
    public void setBinary(String binary) {
        this.binary = binary;
    }

    /**
     * Gets the list nodes that are addressed by transactions created by the Quorum smart contract
     *
     * @return list of bas64 encoded public keys
     */
    public List<String> getPrivateFor() {
        return privateFor;
    }

    /**
     * Sets the list nodes that are addressed by transactions created by the Quorum smart contract
     *
     * @param privateFor list of bas64 encoded public keys
     */
    public void setPrivateFor(List<String> privateFor) {
        this.privateFor = privateFor;
    }

    /**
     * Returns whether or not the contract is considered to be deployed. <br> Note that this method only checks whether
     * the contract info's contract address is set. <br>
     *
     * @return boolean value indicating whether or not the contract is considered to be deployed.
     */
    public boolean isDeployed() {
        return !StringUtil.isNullOrEmpty(contractAddress);
    }

    /**
     * Removes all whitespace and line breaks from the given string. <br> Is meant to reduce the size of pretty printed
     * ABI JSON strings but essentially works on any string.
     *
     * @param abi JSON string containing the ABI
     * @return JSON string containing the ABI but without whitespace and line breaks
     */
    public static String normalizeAbiString(String abi) {
        if (abi == null) throw new IllegalArgumentException("abi must not be null");
        // remove all line breaks and white space
        return abi
                .replaceAll("\r?\n", "")
                .replaceAll("\\h", "");
    }

    /**
     * Parses the given ABI JSON string.
     *
     * @param abi JSON string containing the application binary interface (ABI) of the Quorum smart contract
     * @return parsed ABI
     * @throws IllegalArgumentException in case the string is no valid ABI
     */
    public static AbiDefinition[] parseContractAbi(String abi) {
        try {
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            return objectMapper.readValue(abi, AbiDefinition[].class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse contract abi!", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof QuorumContractInfo)) return false;

        QuorumContractInfo<?> that = (QuorumContractInfo<?>) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(contractAddress, that.contractAddress)
                .append(binary, that.binary)
                .append(abiDefinitions, that.abiDefinitions)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(contractAddress)
                .append(binary)
                .append(abiDefinitions)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("contractAddress", contractAddress)
                .append("binary", binary)
                .toString();
    }
}
