package org.blockchainnative.exceptions;

/**
 * Raised in case an error occurs during execution of a smart contract method.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class ContractCallException extends RuntimeException {

    public ContractCallException() {
        super();
    }

    public ContractCallException(String message) {
        super(message);
    }

    public ContractCallException(Throwable cause) {
        super(cause);
    }

    public ContractCallException(String message, Throwable cause) {
        super(message, cause);
    }


}
