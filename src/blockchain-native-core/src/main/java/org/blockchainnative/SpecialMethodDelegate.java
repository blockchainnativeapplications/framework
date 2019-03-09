package org.blockchainnative;

import org.blockchainnative.metadata.MethodInfo;

import java.util.concurrent.Future;

/**
 * Represents the method to be executed when a specific special method is invoked.
 *
 * @param <TMethodInfo> type of {@code MethodInfo} expected by the delegate
 * @author Matthias Veit
 * @see org.blockchainnative.annotations.ContractMethod
 * @see org.blockchainnative.ContractWrapperGenerator
 * @see org.blockchainnative.AbstractContractWrapper
 * @since 1.0
 */
@FunctionalInterface
public interface SpecialMethodDelegate<TMethodInfo extends MethodInfo> {

    Future<?> invoke(TMethodInfo methodInfo, Object[] args);
}
