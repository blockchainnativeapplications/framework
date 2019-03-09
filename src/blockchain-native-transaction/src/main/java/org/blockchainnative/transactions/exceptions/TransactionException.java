package org.blockchainnative.transactions.exceptions;

import org.blockchainnative.transactions.TransactionRequest;

/**
 * Raised when an error occurs during execution or retrieval of blockchain transactions
 *
 * @see TransactionRequest
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class TransactionException extends BlockchainException {

    public TransactionException() {
        super();
    }

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(Throwable cause) {
        super(cause);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
