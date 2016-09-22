package de.syquel.bushytail.factory.exception;

import de.syquel.bushytail.exception.BushyTailException;

/**
 * Exception for the {@link de.syquel.bushytail.factory.OlingoMetadataFactory}.
 *
 * @author Clemens Bartz
 */
public class OlingoMetadataFactoryException extends BushyTailException {

    public OlingoMetadataFactoryException() {
        super();
    }

    public OlingoMetadataFactoryException(String message) {
        super(message);
    }

    public OlingoMetadataFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public OlingoMetadataFactoryException(Throwable cause) {
        super(cause);
    }
}
