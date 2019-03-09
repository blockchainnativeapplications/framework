package org.blockchainnative.fabric;

/**
 * Defines constants used for defining smart contract wrapper interfaces.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class Constants {

    /**
     * Name of the special method that installs a Hyperledger Fabric chaincode specified in the {@link
     * org.blockchainnative.fabric.metadata.FabricContractInfo} of a smart contract constructed by {@link
     * FabricContractWrapperGenerator}.
     */
    public static final String INSTALL_METHOD = "install";

    /**
     * Name of the special method that creates a Hyperledger Fabric <i>instantiate</i> transaction of the chaincode
     * specified in the {@link org.blockchainnative.fabric.metadata.FabricContractInfo} of a smart contract constructed
     * by {@link FabricContractWrapperGenerator}.
     */
    public static final String INSTANTIATE_METHOD = "instantiate";

    /**
     * Name of the special argument that allows specifying the peers targeted by a method. <br> A parameter declared as such
     * needs to be an {@code Collection} of {@code String} containing the peer names as defined in the network
     * configuration.
     */
    public static final String TARGET_PEERS_ARGUMENT = "targetPeers";

    /**
     * Name of the special argument that allows specifying the user context to be used by a method. <br> A parameter
     * declared as such needs to be an {@link org.hyperledger.fabric.sdk.User}.
     */
    public static final String USER_ARGUMENT = "user";
}
