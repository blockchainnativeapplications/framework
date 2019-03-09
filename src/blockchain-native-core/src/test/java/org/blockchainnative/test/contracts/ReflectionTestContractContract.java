package org.blockchainnative.test.contracts;

import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.annotations.SmartContract;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author Matthias Veit
 */
@SmartContract
public interface ReflectionTestContractContract {

    @ContractMethod
    CompletableFuture<String> getSomeString();

    @ContractMethod
    CompletableFuture<String[][]> getStringArray();

    @ContractMethod
    Future<Set<String[]>> getSetOfStringArrays();

    @ContractMethod
    Future<Set<Set<String[]>>> getSetOfSetsOfStringArrays();

}
