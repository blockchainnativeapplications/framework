package org.blockchainnative.exceptions;

/**
 * Raised in case an error occurs when converting smart contract arguments or result objects.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class TypeConvertException extends RuntimeException {

    public TypeConvertException() {
        super();
    }

    public TypeConvertException(String message) {
        super(message);
    }

    public TypeConvertException(String message, Throwable cause) {
        super(message, cause);
    }


}
