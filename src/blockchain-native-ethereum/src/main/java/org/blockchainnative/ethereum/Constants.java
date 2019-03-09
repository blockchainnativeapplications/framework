package org.blockchainnative.ethereum;

import org.blockchainnative.annotations.EventParameter;

/**
 * Defines constants used for defining smart contract wrapper interfaces.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class Constants {

    /**
     * Special method name that is mapped to a smart contracts constructor classes constructed by {@link EthereumContractWrapperGenerator}.
     */
    public static final String DEPLOYMENT_METHOD = "deploy";

    /**
     * Name of the special argument that allows specifying the gas price used for a transaction in Wei. <br>
     * A parameter declared as such needs to be of type {@code BigInteger} or {@code Number}.
     */
    public static final String GAS_PRICE_ARGUMENT = "gasPrice";

    /**
     * Name of the special argument that allows specifying the gas limit used for a transaction. <br>
     * A parameter declared as such needs to be of type {@code BigInteger} or {@code Number}.
     */
    public static final String GAS_LIMIT_ARGUMENT = "gasLimit";

    /**
     * Name of the special argument that allows specifying the amount of Ether (in Wei) to be sent in a transaction. <br>
     * A parameter declared as such needs to be of type {@code BigInteger} or {@code Number}.
     */
    public static final String WEI_VALUE_ARGUMENT = "value";

    /**
     * Name of the {@link EventParameter} (i.e. special argument) used to define the end block until which events want to be received. <br>
     * A parameter declared as such needs to be of type {@link org.web3j.protocol.core.DefaultBlockParameter}.
     */
    public static final String TO_BLOCK_ARGUMENT = "toBlock";

    /**
     * Name of the {@link EventParameter} (i.e. special argument) used to define the starting block from which events want to be received. <br>
     * A parameter declared as such needs to be of type {@link org.web3j.protocol.core.DefaultBlockParameter}.
     */
    public static final String FROM_BLOCK_ARGUMENT = "fromBlock";
}
