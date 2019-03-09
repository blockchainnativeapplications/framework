package org.blockchainnative.exceptions;

/**
 * Raised in case an error occurs during creation of a smart contract wrapper.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class ContractWrapperCreationException extends RuntimeException {

    public ContractWrapperCreationException() {
        super();
    }

    public ContractWrapperCreationException(String message) {
        super(message);
    }

    public ContractWrapperCreationException(Throwable cause) {
        super(cause);
    }

    public ContractWrapperCreationException(String message, Throwable cause) {
        super(message, cause);
    }


}
