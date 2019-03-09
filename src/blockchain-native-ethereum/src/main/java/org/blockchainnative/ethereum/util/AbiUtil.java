package org.blockchainnative.ethereum.util;


/**
 * Provides static utility methods for working with application binary interface (ABI) strings..
 * <br>
 * The class is not intended to be instantiated as it only provides static methods.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public final class AbiUtil {

    private AbiUtil() {
    }

    /**
     * Returns the solidity type name but strips the location suffixes 'storage', 'memory' and 'calldata'.
     *
     * @param type field type as defined in the contract ABI
     * @return stripped down type name
     */
    public static String stripLocationFromType(String type) {
        if (type.endsWith(" storage") || type.endsWith(" memory") || type.endsWith(" calldata"))
            return type.split(" ")[0];
        return type;
    }
}
