package org.blockchainnative.serialization;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @since 1.0
 * @author Matthias Veit
 */
public abstract class MethodInfoMixin {

    @JsonIgnore
    public abstract boolean isAsync();

    @JsonIgnore
    public abstract boolean isVoidReturnType();
}
