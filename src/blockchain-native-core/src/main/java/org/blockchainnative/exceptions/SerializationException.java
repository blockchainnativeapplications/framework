package org.blockchainnative.exceptions;

/**
 * Raised in of errors during serialization of {@code ContractInfo} objects
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class SerializationException extends RuntimeException {

    public SerializationException() {
        super();
    }

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }


}
