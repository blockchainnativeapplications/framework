package org.blockchainnative.exceptions;

/**
 * Raised in case an error occurs during deployment of a smart contract.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public class ContractDeploymentException extends ContractCallException {

    public ContractDeploymentException() {
        super();
    }

    public ContractDeploymentException(String message) {
        super(message);
    }

    public ContractDeploymentException(Throwable cause) {
        super(cause);
    }

    public ContractDeploymentException(String message, Throwable cause) {
        super(message, cause);
    }


}
