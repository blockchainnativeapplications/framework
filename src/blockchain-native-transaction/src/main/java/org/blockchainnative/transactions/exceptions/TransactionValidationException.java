package org.blockchainnative.transactions.exceptions;

import org.blockchainnative.transactions.TransactionRequest;

/**
 * Raised when an error occurs during validation of blockchain transactions
 *
 * @see TransactionRequest
 *
 * @author Matthias Veit
 * @since 1.1
 */
public class TransactionValidationException extends TransactionException {

    public TransactionValidationException() {
        super();
    }

    public TransactionValidationException(String message) {
        super(message);
    }

    public TransactionValidationException(Throwable cause) {
        super(cause);
    }

    public TransactionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
