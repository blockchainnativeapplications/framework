package org.blockchainnative.transactions.exceptions;

/**
 * Base exception representing blockchain related errors
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class BlockchainException extends RuntimeException {

    public BlockchainException() {
        super();
    }

    public BlockchainException(String message) {
        super(message);
    }

    public BlockchainException(Throwable cause) {
        super(cause);
    }

    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
    }
}
