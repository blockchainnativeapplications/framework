package org.blockchainnative.test.contracts;

import org.blockchainnative.annotations.ContractMethod;
import org.blockchainnative.annotations.SmartContract;
import org.blockchainnative.metadata.Result;

import java.util.concurrent.Future;

/**
 * @author Matthias Veit
 */
@SmartContract
public interface HelloContractWithBlockInformation {

    @ContractMethod
    Result<String> hello(String name);

    @ContractMethod("hello")
    Future<Result<String>> helloAsync(String name);

    @ContractMethod(value = "hello", isReadOnly = true)
    Future<Result<String>> helloReadOnly(String name);
}
