package org.blockchainnative.fabric;

import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

import java.io.IOException;

/**
 * Provides static utility methods specific to Hyperledger Fabric and its SDK.
 * <br>
 * The class is not intended to be instantiated as it only provides static methods.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public final class FabricUtil {

    private FabricUtil() {
    }

    /**
     * Calculates the hash value of block from its {@code BlockEvent} using {@link SDKUtils#calculateBlockHash(HFClient,
     * long, byte[], byte[])}.
     *
     * @param client     Hyperledger Fabric client
     * @param blockEvent event whose block hash should be calculated
     * @return hash of the block containing the {@code BlockEvent} as hex string
     */
    public static String getBlockHashFromEvent(HFClient client, BlockEvent blockEvent) {
        var blockHash = new byte[0];
        try {
            blockHash = SDKUtils.calculateBlockHash(
                    client,
                    blockEvent.getBlockNumber(),
                    blockEvent.getBlock().getHeader().getPreviousHash().toByteArray(),
                    blockEvent.getDataHash());
        } catch (IOException | InvalidArgumentException e) {
            throw new RuntimeException("Failed to calculate block hash", e);
        }

        return Hex.encodeHexString(blockHash);
    }
}
