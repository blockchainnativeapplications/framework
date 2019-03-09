package org.blockchainnative.quorum;

/**
 * Defines constants used for defining smart contract wrapper interfaces.
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class Constants extends org.blockchainnative.ethereum.Constants {
    /**
     * Name of the special argument that allows specifying the recipients of a transaction. <br>
     * A parameter declared as such needs to be of type {@code Collection<String>} or its subtypes.
     */
    public static final String PRIVATE_FOR_ARGUMENT = "privateFor";
}
